package com.balaji.naga.resource;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.utils.ProjectMessages;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Orchestration {
    private Map<Long, Thread> processIDToThread = new HashMap<>();
    private Map<Long, Process> processIDToProcess = new HashMap<>();
    private Map<Long, Channel> channelIDToChannel = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Orchestration.class.getName());

    private void createProcessInstances(int noOfProcess, long initialValue) {
        for (long i = 0; i < noOfProcess; i ++) {
            long processID = i + 1;
            processIDToProcess.put(processID, new Process(processID, initialValue));
        }
    }

    private void createChannels(int noOfProcess, Channel.ChannelType type) {
        long channelID = 0;
        for (long i = 0; i < noOfProcess; i ++) {
            long processID = i + 1;
            Process process = processIDToProcess.get(processID);
            for (long j = 0; j < noOfProcess; j ++) {
                if (i == j) {
                    continue;
                }
                ++channelID;
                long toProcessID = j + 1;
                Channel channel = Channel.createChannel(type, process, processIDToProcess.get(toProcessID));

                process.associateWriteChannel(channel);
                processIDToProcess.get(toProcessID).associateReadChannel(channel);

                this.channelIDToChannel.put(channelID, channel);
            }
        }
    }

    private void bootProcesses() {
        try {
            for (Process process : getAllProcesses()) {
                Thread thread = process;

                process.start();
                processIDToThread.put(process.getProcessID(), thread);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "bootProcess()", e);
        }
    }

    /*
     * TODO : Process creation and association can be done in a better way
     */
    public void createProcess(int noOfProcess, long initialValue, Channel.ChannelType type) {
        this.createProcessInstances(noOfProcess, initialValue);
        this.createChannels(noOfProcess, type);
        this.bootProcesses();
    }

    public void sendMessage (Message message) {
        processIDToProcess.get(message.getFrom()).sendMessage(message);

    }

    public Collection<Process> getAllProcesses() {
        return processIDToProcess.values();
    }

    public Process getProcess(Long processID) {
        return processIDToProcess.get(processID);
    }

    public Collection<Channel> getAllChannels() {
        return channelIDToChannel.values();
    }

    public int getTotalProcess() {
        return processIDToProcess.size();
    }

    public String toStringProcesses() {
        StringBuilder builder = new StringBuilder();

        builder.append("Processes : ");
        builder.append(ProjectMessages.NEWLINE);

        for (Process process : this.getAllProcesses()) {
            builder.append(process.toString());
            builder.append(ProjectMessages.NEWLINE);
        }

        return builder.toString();
    }

    public String toStringChannel() {
        StringBuilder builder = new StringBuilder();

        builder.append("Channels : ");
        builder.append(ProjectMessages.NEWLINE);

        for (Channel channel : this.getAllChannels()) {
            builder.append(channel.toString());
            builder.append(ProjectMessages.NEWLINE);
        }

        return builder.toString();
    }

    @Override
    public String toString(){
        return new StringBuilder()
                .append(ProjectMessages.NEWLINE)
                .append(toStringProcesses())
                .append(ProjectMessages.NEWLINE)
                .append(ProjectMessages.NEWLINE)
                .append(toStringChannel())
                .toString();
    }
}
