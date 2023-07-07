package org.distributed.lock.config;

import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

public class ZooKeeperServerClient {

    private ZooConfig zooConfig;
    private ZooKeeper zoo;



    public ZooKeeperServerClient(ZooConfig zooConfig) {
        this.zooConfig = zooConfig;
    }

    public ZooKeeper getServer(){
        if(zoo == null ){
            try{
                zoo = new ZooKeeper(zooConfig.getHost(),zooConfig.getTimeOut(),zooConfig.getWatcher());
            }catch(IOException exception){
                System.out.println("Exception occurred while establishing the connection");
                throw new RuntimeException("Exception occurred");
            }
        }
        return zoo;
    }
}
