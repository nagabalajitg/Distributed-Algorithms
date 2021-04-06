package com.balaji.naga.resource.communication;

import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.utils.Messages;

import java.util.logging.Logger;

public abstract class AbstractChannel implements Channel{
    private String id;
    private Process process1;
    private Process process2;
    private static final Logger LOGGER = Logger.getLogger(AbstractChannel.class.getName());

    AbstractChannel (Process process1, Process process2) {
        this.id = process1.getProcessID() +":"+process2.getProcessID();
        this.process1 = process1;
        this.process2 = process2;
    }

    @Override
    public String getChannelID() {
        return this.id;
    }

    @Override
    public Process getProcess1() {
        return process1;
    }

    @Override
    public Process getProcess2  () {
        return process2;
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("Channel ID : ")
                .append(getChannelID())
                .append(Messages.SPACE)
                .append("and Processes are ")
                .append(process1.getProcessID())
                .append(" and ")
                .append(process2.getProcessID())
                .toString();
    }

}
