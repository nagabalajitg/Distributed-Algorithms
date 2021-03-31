package com.balaji.naga.resource.communication;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.compute.Process;

import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Logger;

public class FifoChannel extends AbstractChannel {
    private Queue<Message> queue;
    private static final Logger LOGGER = Logger.getLogger(FifoChannel.class.getName());

    public FifoChannel(Process process1, Process process2) {
        super(process1, process2);
        queue = new LinkedList<>();
    }

    @Override
    public boolean hasMessage() {
        synchronized (queue) {
            return queue.size() > 0;
        }
    }

    @Override
    public Message readMessage() {
        Message message;
        synchronized (queue) {
            message = queue.poll();
        }
        return message;
    }

    @Override
    public void sendMessage(Message message) {
        synchronized (queue) {
            queue.offer(message);
        }
    }
}
