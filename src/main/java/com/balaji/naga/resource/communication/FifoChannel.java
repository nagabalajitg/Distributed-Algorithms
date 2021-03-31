package com.balaji.naga.communication;

import com.balaji.naga.compute.Process;

public class FifoChannel extends AbstractChannel {

    public FifoChannel(Process process1, Process process2) {
        super(process1, process2);
    }

    @Override
    public void addMessage(String message) {}
}
