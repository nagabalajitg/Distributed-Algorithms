package com.balaji.naga.resource;

import com.balaji.naga.message.Message;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.compute.Process;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ResourceOrchestration {
    private Map<Long, Process> processIDToProcess = new HashMap<>();
    private static final ResourceOrchestration ORCHESTRATION = new ResourceOrchestration();
    private static final Logger LOGGER = Logger.getLogger(ResourceOrchestration.class.getName());

    private ResourceOrchestration() {}

    public static ResourceOrchestration getOrchestration() {
        return ORCHESTRATION;
    }
    /*
     * TODO : Process creation and association can be done in a better way
     */
    public void createProcess(int noOfProcess, long initialValue, Channel.ChannelType type) {
        for (long i = 0; i < noOfProcess; i ++) {
            long processID = i + 1;
            processIDToProcess.put(processID, new Process(processID, initialValue));
        }
        for (long i = 0; i < noOfProcess; i ++) {
            long processID = i + 1;
            Process process = processIDToProcess.get(processID);
            for (long j = 0; j < noOfProcess; j ++) {
                if (i == j) {
                    continue;
                }
                long toProcessID = j + 1;
                process.associateChannel(toProcessID, Channel.createChannel(type, process, processIDToProcess.get(toProcessID)));
            }
        }
    }

    public void sendMessage (Message message, long processID_1, long processID_2) {
        processIDToProcess.get(processID_1).sendMessage(message, processID_2);
    }

    public int getTotalProcess() {
        return processIDToProcess.size();
    }
}
