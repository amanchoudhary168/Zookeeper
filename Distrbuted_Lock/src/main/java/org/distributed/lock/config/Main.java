package org.distributed.lock.config;


import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.util.UUID;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) throws InterruptedException {
       ZooConfig config = new ZooConfig("localhost",10000);
       ZooKeeperServerClient client = new ZooKeeperServerClient(config);
       Lock lock = new Lock(client);
       String sessionId = UUID.randomUUID().toString();
        try {
            boolean lockAcquired = lock.getLock(sessionId);
            System.out.println(lockAcquired);
        } catch (InterruptedException |KeeperException e) {
            //throw new RuntimeException(e);
        }




    }
}