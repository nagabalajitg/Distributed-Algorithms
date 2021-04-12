package com.balaji.naga.resource;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.communication.Channel.ChannelType;
import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.utils.Messages;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Orchestration {
    private Map<Long, Thread> processIDToThread = new HashMap<>();
    private Map<Long, Process> processIDToProcess = new HashMap<>();
    private Map<Long, Channel> channelIDToChannel = new HashMap<>();
    private static final Logger LOGGER = Logger.getLogger(Orchestration.class.getName());

    public static class ProcessInitMeta {
        private int noOfProcesses;
        private List<Long> initialValueList;

        public  enum ProcessInitMetaType{
            FIXED(1), VARIABLE(2);

            private int type;

            ProcessInitMetaType(int type) {
                this.type = type;
            }

            public int getMetaType() {
                return this.type;
            }

            public static ProcessInitMetaType getTypeFromInt(int type) {
                for(ProcessInitMetaType metaType : ProcessInitMetaType.values()) {
                    if (metaType.getMetaType() == type) {
                        return metaType;
                    }
                }
                throw new IllegalArgumentException(Messages.INVALID_TYPE);
            }
        }

        public ProcessInitMeta(int noOfProcesses) {
            this.noOfProcesses = noOfProcesses;
            this.initialValueList = new ArrayList<>(noOfProcesses);
        }

        public void addInitialValue(long initialValue) {
            if (this.initialValueList.size() < noOfProcesses) {
                this.initialValueList.add(initialValue);
            }
        }

        public int getTotalNoOfProcess() {
            return this.noOfProcesses;
        }

        public long getInitialValueOf(long nthProcess) {
            int intValue = (int) nthProcess - 1;
            return this.initialValueList.get(intValue).longValue();
        }
        public Iterator<Long> iterateInitialValues() {
            return this.initialValueList.iterator();
        }
    }

    protected Process createAProcess(long processID, long initialValue) {
        return new Process(processID, initialValue);
    }

    private void createProcessInstances(ProcessInitMeta meta) {
        Iterator<Long> iterator = meta.iterateInitialValues();
        for (long i = 0; i < meta.getTotalNoOfProcess(); i ++) {
            long processID = i + 1;
            processIDToProcess.put(processID, createAProcess(processID, iterator.next()));
        }
    }

    private void createProcessInstances(int noOfProcess, long initialValue) {
        for (long i = 0; i < noOfProcess; i ++) {
            long processID = i + 1;
            processIDToProcess.put(processID, createAProcess(processID, initialValue));
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

    public void createProcess(int noOfProcess, long initialValue, ChannelType type) {
        this.createProcessInstances(noOfProcess, initialValue);
        this.createChannels(noOfProcess, type);
        this.bootProcesses();
    }

    public void createProcess(ProcessInitMeta meta, ChannelType type) {
        this.createProcessInstances(meta);
        this.createChannels(meta.getTotalNoOfProcess(), type);
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

    public void shutdownAllProcess() {
        try {
            for(Thread thread : processIDToThread.values()) {
                thread.destroy();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public String toStringProcesses() {
        StringBuilder builder = new StringBuilder();

        builder.append("Processes : ");
        builder.append(Messages.NEWLINE);

        for (Process process : this.getAllProcesses()) {
            builder.append(process.toString());
            builder.append(Messages.NEWLINE);
        }

        return builder.toString();
    }

    public String toStringChannel() {
        StringBuilder builder = new StringBuilder();

        builder.append("Channels : ");
        builder.append(Messages.NEWLINE);

        for (Channel channel : this.getAllChannels()) {
            builder.append(channel.toString());
            builder.append(Messages.NEWLINE);
        }

        return builder.toString();
    }

    @Override
    public String toString(){
        return new StringBuilder()
                .append(Messages.NEWLINE)
                .append(toStringProcesses())
                .append(Messages.NEWLINE)
                .append(Messages.NEWLINE)
                .append(toStringChannel())
                .toString();
    }
}
