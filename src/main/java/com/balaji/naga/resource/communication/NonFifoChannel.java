package com.balaji.naga.resource.communication;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.compute.Process;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.logging.Logger;

public class NonFifoChannel extends AbstractChannel {
    private Random random;
    private Queue<Pair<Integer, Message>> queue;
    private static final Logger LOGGER = Logger.getLogger(NonFifoChannel.class.getName());

    private static class Pair<k, v> {
        k key;
        v value;

        Pair (k key, v value) {
            this.key = key;
            this.value = value;
        }

        k getKey() { return  this.key; }
        v getValue() {
            return this.value;
        }
    }

    public NonFifoChannel(Process process1, Process process2) {
        super(process1, process2);
        this.random = new Random();
        this.queue = new PriorityQueue<>((Pair<Integer, Message> a, Pair<Integer, Message> b) -> a.getKey() - b.getKey());
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
            Pair<Integer, Message> pair = queue.poll();
            message = pair.getValue();
        }
        return message;
    }

    @Override
    public void sendMessage(Message message){
        int key = random.nextInt();
        synchronized (queue) {
            queue.offer(new Pair<Integer, Message>(key, message));
        }
    }
}
