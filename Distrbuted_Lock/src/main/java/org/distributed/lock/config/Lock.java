package org.distributed.lock.config;

import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Lock {
    private ZooKeeperServerClient zk;
    private static final String ROOT_NODE = "/locks";

    Logger log = LoggerFactory.getLogger(Lock.class.getSimpleName());

    public Lock(ZooKeeperServerClient zk) {
        this.zk = zk;
        try {
            ZooKeeper zooKeeper = zk.getServer();

            if(zooKeeper.exists(ROOT_NODE,false) == null){
                zooKeeper.create(ROOT_NODE,new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean getLock(String sessionId) throws InterruptedException, KeeperException {

        ZooKeeper server = zk.getServer();
        server.create(ROOT_NODE+"/"+"lock-",sessionId.getBytes(StandardCharsets.UTF_8),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,CreateMode.EPHEMERAL_SEQUENTIAL);
        List<String> children = server.getChildren(ROOT_NODE,false);
        children.sort(String::compareTo);
        byte[] data = server.getData(ROOT_NODE + "/" + children.get(0), false, null);
        if(data != null && new String(data).equalsIgnoreCase(sessionId)){
            log.info("Lock acquired successfully");
            for(int i=0;i<20;i++){
                Thread.sleep(2000);
                log.info("Lock will be released in sometime");
            }
            releaseLock(sessionId);
            log.info("Lock released successfully");
            return true;
        }
        Semaphore semaphore = new Semaphore(0);
        server.getChildren(ROOT_NODE,new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                if(event.getType() == Event.EventType.NodeChildrenChanged){
                    try {
                        getLockRetry(sessionId,semaphore);
                    } catch (InterruptedException | KeeperException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        log.info("-----Waiting for lock------");
        semaphore.acquire();
        return false;
    }

    public void getLockRetry(String sessionId, Semaphore semaphore) throws InterruptedException, KeeperException {

        ZooKeeper server = zk.getServer();
        List<String> children = server.getChildren(ROOT_NODE,false);
        children.sort(String::compareTo);
        byte[] data = server.getData(ROOT_NODE + "/" + children.get(0), false, null);
        if(data != null && new String(data).equalsIgnoreCase(sessionId)){
            log.info("Lock acquired successfully");
            for(int i=0;i<20;i++){
                Thread.sleep(2000);
                log.info("Lock will be released in sometime");
            }
            releaseLock(sessionId);
            log.info("Lock released successfully");
            return;
        }
        log.info("!!!!!!!Waiting for lock!!!!!!!");
        server.getChildren(ROOT_NODE,new Watcher() {
            @Override
            public void process(WatchedEvent event) {
                log.info("----------Executing watcher---------");
                if(event.getType() == Event.EventType.NodeChildrenChanged){
                    try {
                        getLockRetry(sessionId,semaphore);
                        semaphore.release();
                    } catch (InterruptedException | KeeperException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        log.info("----Watch Added---");
    }

    public void releaseLock(String sessionId) throws InterruptedException, KeeperException {
        ZooKeeper server = zk.getServer();
        List<String> children = server.getChildren(ROOT_NODE,false);
        children.sort(String::compareTo);
        for(String child: children ){
            byte data[] = server.getData(ROOT_NODE + "/" + child, false, null);
            if(data != null && new String(data).equalsIgnoreCase(sessionId)){
                log.info("Releasing the lock");
                server.delete(ROOT_NODE+"/"+children.get(0),-1);
                return;
            }
        }
        log.info("No locks found for the sessionId");

    }


}
