package com.balaji.naga.compute;

import com.balaji.naga.communication.Channel;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Process extends Thread {
    private long id;
    private Set<Channel> chennels;
    private Queue<Long> waitingQueue;

    Process(long id) {
        this.id = id;
        this.waitingQueue = new PriorityQueue<>();
    }

    Process() {
        this(System.currentTimeMillis());
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