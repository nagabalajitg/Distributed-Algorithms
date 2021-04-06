package com.balaji.naga.resource.compute;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.utils.Messages;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Process extends Thread {
    private long id;
    private AtomicLong data;
    private Map<Long, Channel> readChannels;
    private Map<Long, Channel> writeChannels;

    private static final Logger LOGGER = Logger.getLogger(Process.class.getName());

    public Process(long id, Long data) {
        this.id = id;
        this.data = new AtomicLong(data);
        this.readChannels = new ConcurrentHashMap<>();
        this.writeChannels = new ConcurrentHashMap<>();
    }

    public Long getLocalState() { return this.data.get(); }

    public void updateData(Long data) {
        this.data.addAndGet(data);
    }

    public long getProcessID() {
        return this.id;
    }

    public Collection<Channel> getAllWriteChannels() {
        return this.writeChannels.values();
    }

    public Collection<Channel> getAllReadChannels() {
        return this.readChannels.values();
    }

    public Channel getWriteChannelForProcess(Long processID) {
        Channel channel = this.writeChannels.get(processID);
        if (channel == null) {
            throw new IllegalArgumentException(Messages.INVALID_PROCESS_ID_FOR_WRITE_CHANNEL);
        }
        return channel;
    }

    public Channel getReadChannelForProcess(Long processID) {
        Channel channel = this.writeChannels.get(processID);
        if (channel == null) {
            throw new IllegalArgumentException(Messages.INVALID_PROCESS_ID_FOR_WRITE_CHANNEL);
        }
        return channel;
    }

    @Override
    public void run () {
        try {
            while (true) {
                for (Channel channel : readChannels.values()) {
                    if (channel.hasMessage()) {
                        channel.readMessage().processAtReceiver(this, channel);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Process - run()", e);
        }
    }

    public void associateWriteChannel(Channel channel) {
        this.writeChannels.put(channel.getProcess2().getProcessID(), channel);
    }

    public void associateReadChannel(Channel channel) {
        this.readChannels.put(channel.getProcess1().getProcessID(), channel);
    }

    public void sendMessage(Message message) {
        message.processAtSender(this, getWriteChannelForProcess(message.getTo()));
    }

    @Override
    public boolean equals(Object process) {
        return process != null
                && Process.class.isInstance(process)
                && this.id == ((Process)process).getProcessID();
    }

    @Override
    public String toString() {
        return new StringBuilder("Process ID : ")
                .append(getProcessID())
                .append(Messages.SPACE)
                .append("and")
                .append(Messages.SPACE)
                .append("Data : ")
                .append(Messages.SPACE)
                .append(getLocalState())
                .toString();
    }
}