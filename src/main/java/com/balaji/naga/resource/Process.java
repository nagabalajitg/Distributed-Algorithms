package com.balaji.naga.resource;

import com.balaji.naga.communication.Channel;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Process extends Thread implements Resource {
    private long id;
    private Site site;
    private Set<Channel> chennels;
    private Queue<Long> waitingQueue;

    private void init() {
        this.waitingQueue = new PriorityQueue<>();
    }

//    Process (Site site) {
//        this.site = site;
//        this.id = System.currentTimeMillis();
//        this.init();
//    }
//
//    Process (long id, Site site) {
//        this.id = id;
//        this.site = site;
//        this.init();
//    }


    Process(long id) {
        this.id = id;
        this.init();
    }


    long getProcessID () {
        return this.id;
    }

    @Override
    public void run () {
        while (true) {
            synchronized (this) {

            }
        }
    }

    public void addChannels (Set<Channel> channels) {
        synchronized (this) {
            this.chennels.addAll(chennels);
        }
    }

    public boolean equals(Process process) {
        return process != null && this.id == process.getProcessID();
    }
}