package com.balaji.naga.communication;

import com.balaji.naga.resource.Group;
import com.balaji.naga.compute.Process;
import com.balaji.naga.resource.Site;

import java.util.Random;

public class NonFifoChannel extends AbstractChannel {
    private Random random;

    public NonFifoChannel(Process process1, Process process2) {
        super(process1, process2);
        this.random = new Random();
    }

    @Override
    public void addMessage(String message){}
}
