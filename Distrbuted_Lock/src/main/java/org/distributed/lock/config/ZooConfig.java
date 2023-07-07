package org.distributed.lock.config;

import org.apache.zookeeper.Watcher;

public class ZooConfig {

    private String host;
    private int timeOut;

    private Watcher watcher;


    public ZooConfig(String host,int timeOut){
        this.host = host;
        this.timeOut = timeOut;
    }
    public ZooConfig(String host,int timeOut,Watcher watcher){
        this(host,timeOut);
        this.watcher = watcher;
    }

    public String getHost() {
        return host;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public Watcher getWatcher() {
        return watcher;
    }
}
