package com.balaji.naga.snapshot;


import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog;
import com.balaji.naga.message.LaiYangMessage;
import com.balaji.naga.message.Message;
import com.balaji.naga.utils.KeyValue;
import com.balaji.naga.utils.Messages;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class LaiYangSnapshot implements Snapshot {
    private final AtomicBoolean isInProgress;
    private boolean isInGlobalConsistentState = Boolean.TRUE;

    private Long timestamp;
    private Long initiatorProcessID;
    private AtomicLong recordedLocalState;
    private Map<String, List<WhiteMessageLog>> messagesSentAtInitiator;
    private Map<String, List<WhiteMessageLog>> messagesReceivedAtInitiator;

    private Map<Long, Long> otherProcessIDToLocalState;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageSent;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageReceived;

    private List<ComputedProcessStateBean> computedProcessStateList;

    private static class ComputedProcessStateBean {
        private long currentProcess;
        private long initiatedProcess;
        private boolean isInConsistentState;

        private long messageInTransitFromInitiatorToCurrentProcess;
        private long messageInTransitFromCurrentProcessToInitiator;

        public void setInitiatedProcess(long initiatedProcess) {
            this.initiatedProcess = initiatedProcess;
        }

        public long getInitiatedProcess() {
            return this.initiatedProcess;
        }

        public void setCurrentProcess(long currentProcess) {
            this.currentProcess = currentProcess;
        }

        public long getCurrentProcess() {
            return this.currentProcess;
        }

        public void setConsistentState(boolean state) {
            this.isInConsistentState = state;
        }

        public boolean getConsistentState() {
            return this.isInConsistentState;
        }

        public void setMessageInTransitFromInitiatorToCurrentProcess(long message) {
            this.messageInTransitFromInitiatorToCurrentProcess = message;
        }

        public long getMessageInTransitFromInitiatorToCurrentProcess() {
            return this.messageInTransitFromInitiatorToCurrentProcess;
        }

        public void setMessageInTransitFromCurrentProcessToInitiator(long message) {
            this.messageInTransitFromCurrentProcessToInitiator = message;
        }

        public long getMessageInTransitFromCurrentProcessToInitiator() {
            return this.messageInTransitFromInitiatorToCurrentProcess;
        }
    }



    public LaiYangSnapshot(long timestamp, Long localState, Long initiatorProcessID) {
        this.timestamp = timestamp;
        this.isInProgress = new AtomicBoolean(true);
        this.initiatorProcessID = initiatorProcessID;
        this.recordedLocalState = new AtomicLong(localState);
        this.computedProcessStateList  = new LinkedList<>();

        this.messagesSentAtInitiator = new HashMap<>();
        this.messagesReceivedAtInitiator = new HashMap<>();

        this.channelIDToMessageSent = new HashMap<>();
        this.channelIDToMessageReceived = new HashMap<>();
        this.otherProcessIDToLocalState = new HashMap<>();
    }

    public void setSentMessage(String channelID, List<WhiteMessageLog> messages) {
        this.messagesSentAtInitiator.put(channelID, messages);
    }

    public void setReceivedMessage(String channelID, List<WhiteMessageLog> messages) {
        this.messagesReceivedAtInitiator.put(channelID, messages);
    }

    public boolean isInProgress() {
        synchronized (this) {
            return isInProgress.get();
        }
    }

    public void setIsInProgress(boolean isInProgress) {
        synchronized (this) {
            this.isInProgress.set(isInProgress);
        }
    }

    public Long getInitiatorProcessID () {
        return this.initiatorProcessID;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Long getLocalState() {
        return recordedLocalState.get();
    }

    public void setLocalState(Long localState) {
        recordedLocalState.set(localState);
    }

    private void computeGlobalState() {
        Long initiatedProcessID = this.getInitiatorProcessID();
        List<KeyValue<Long, Boolean>> processStates = new LinkedList<>();

        for (Map.Entry<Long, Long> entry : otherProcessIDToLocalState.entrySet()) {
            Long currentProcessID = entry.getKey();
            ComputedProcessStateBean bean = new ComputedProcessStateBean();

            Long inTransit_Sent_InitiatorToOther = 0l;
            Long inTransit_Received_InitiatorFromOther = 0l;

            Long inTransit_Sent_OtherToInitiator = 0l;
            Long inTransit_Received_OtherFromInitiator = 0l;


            Iterator<WhiteMessageLog> iterator = this.messagesSentAtInitiator.get(initiatedProcessID+ ":"+currentProcessID).iterator();
            while (iterator.hasNext()) {
                inTransit_Sent_InitiatorToOther += iterator.next().getData();
            }

            iterator = this.messagesReceivedAtInitiator.get(currentProcessID+ ":"+initiatedProcessID).iterator();
            while (iterator.hasNext()) {
                inTransit_Received_InitiatorFromOther += iterator.next().getData();
            }


            iterator = this.channelIDToMessageSent.get(currentProcessID+ ":"+initiatedProcessID).iterator();
            while (iterator.hasNext()) {
                inTransit_Sent_OtherToInitiator += iterator.next().getData();
            }

            iterator = this.channelIDToMessageReceived.get(initiatedProcessID+ ":"+currentProcessID).iterator();
            while (iterator.hasNext()) {
                inTransit_Received_OtherFromInitiator += iterator.next().getData();
            }

            Long initiatorToProcessDiff = inTransit_Sent_InitiatorToOther - inTransit_Received_OtherFromInitiator;
            Long processToInitiatorDiff = inTransit_Sent_OtherToInitiator - inTransit_Received_InitiatorFromOther;


            boolean localConsistentState = initiatorToProcessDiff < 0 || processToInitiatorDiff < 0;
            processStates.add(new KeyValue(currentProcessID, localConsistentState));

            this.isInGlobalConsistentState = this.isInGlobalConsistentState && localConsistentState;

            bean.setCurrentProcess(currentProcessID);
            bean.setInitiatedProcess(initiatedProcessID);
            bean.setConsistentState(localConsistentState);
            bean.setMessageInTransitFromInitiatorToCurrentProcess(initiatorToProcessDiff);
            bean.setMessageInTransitFromCurrentProcessToInitiator(processToInitiatorDiff);
            computedProcessStateList.add(bean);
        }
    }

    @Override
    public int totalNoOfRecordedProcess() {
        return otherProcessIDToLocalState.size();
    }

    @Override
    public void processMessage(Message message) {
        LaiYangMessage msg = (LaiYangMessage) message;
        otherProcessIDToLocalState.put(msg.getFrom(), msg.getProcessLocalState());
        channelIDToMessageSent.put(msg.getFrom() + ":" + msg.getTo(), msg.getMessageSent());
        channelIDToMessageReceived.put(msg.getTo() + ":" + msg.getFrom(), msg.getMessageReceived());
        if (otherProcessIDToLocalState.size() == messagesSentAtInitiator.size()) {
            computeGlobalState();
            setIsInProgress(Boolean.FALSE);
        }
    }

    public Long accumulateLocalAndInTransitStateMessages() {
        Long result = getLocalState();
        Long mainProcess = getInitiatorProcessID();

        for (Map.Entry<Long, Long> entry : otherProcessIDToLocalState.entrySet()) {
            Long inTransitMessage = 0l;
            Long otherProcessID = entry.getKey();

            Iterator<WhiteMessageLog> iterator = this.messagesSentAtInitiator.get(mainProcess+ ":"+otherProcessID).iterator();
            while (iterator.hasNext()) {
                inTransitMessage += iterator.next().getData();
            }

            iterator = this.channelIDToMessageReceived.get(mainProcess+ ":"+otherProcessID).iterator();
            while (iterator.hasNext()) {
                inTransitMessage -= iterator.next().getData();
            }

            iterator = this.channelIDToMessageSent.get(otherProcessID+ ":"+mainProcess).iterator();
            while (iterator.hasNext()) {
                inTransitMessage += iterator.next().getData();
            }

            iterator = this.messagesReceivedAtInitiator.get(otherProcessID+ ":"+mainProcess).iterator();
            while (iterator.hasNext()) {
                inTransitMessage -= iterator.next().getData();
            }

            result += (entry.getValue() + inTransitMessage);
        }

        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Long initiatedProcessID = this.getInitiatorProcessID();

        boolean isInGlobalConsistentState = Boolean.TRUE;
        List<KeyValue<Long, Boolean>> processStates = new LinkedList<>();

        builder.append("Snapshot taken at ").append(this.getTimestamp());
        builder.append(Messages.NEWLINE);
        builder.append("Initiated ID : ").append(initiatedProcessID);
        builder.append(Messages.SPACE);
        builder.append("Process State : ").append(this.getLocalState());
        builder.append(Messages.NEWLINE);
        builder.append(Messages.NEWLINE);

        for (ComputedProcessStateBean bean : computedProcessStateList) {
            Long currentProcessID = bean.getCurrentProcess();
            Long initiatorToProcessDiff = bean.getMessageInTransitFromInitiatorToCurrentProcess();
            Long processToInitiatorDiff = bean.getMessageInTransitFromCurrentProcessToInitiator();

            builder.append("The local state at process ")
                    .append(bean.getCurrentProcess())
                    .append(" is ")
                    .append(otherProcessIDToLocalState.get(bean.getCurrentProcess()));

            if (initiatorToProcessDiff > 0) {
                builder.append("In-transit message from ")
                        .append(initiatedProcessID)
                        .append(" to ")
                        .append(currentProcessID)
                        .append(" is ")
                        .append(initiatorToProcessDiff);
                builder.append(Messages.NEWLINE);

            }
            if (processToInitiatorDiff > 0) {
                builder.append("In-transit message from ")
                        .append(currentProcessID)
                        .append(" to ")
                        .append(initiatedProcessID)
                        .append(" is ")
                        .append(processToInitiatorDiff);
                builder.append(Messages.NEWLINE);
            }

            if (!bean.getConsistentState()) {
                builder.append("State between process ")
                        .append(initiatedProcessID)
                        .append(" and ")
                        .append(currentProcessID)
                        .append(" is not consistent");
                builder.append(Messages.NEWLINE);
            }
        }

        builder.append(Messages.NEWLINE);

        if (isInGlobalConsistentState) {
            builder.append(Messages.PROCESSES_ARE_IN_GLOBAL_CONSISTENT_STATE);
        } else {
            builder.append(Messages.PROCESSES_ARE_NOT_IN_GLOBAL_CONSISTENT_STATE);
        }

        return builder.toString();
    }
}
