package com.balaji.naga.snapshot;


import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog;
import com.balaji.naga.message.LaiYangMessage;
import com.balaji.naga.message.Message;
import com.balaji.naga.utils.KeyValue;
import com.balaji.naga.utils.Messages;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class LaiYangSnapshot implements Snapshot {
    private final AtomicBoolean isInProgress;
    private boolean isInGlobalConsistentState = Boolean.TRUE;

    private Date date;
    private Long timestamp;
    private Long initiatorProcessID;
    private Long totalNoOfProcesses;
    private AtomicLong recordedLocalState;
    private Map<String, List<WhiteMessageLog>> initiator_channelID_MessagesSent;
    private Map<String, List<WhiteMessageLog>> initiator_channelID_MessagesReceived;

    private Map<Long, Long> otherProcessIDToLocalState;
    private Map<String, List<WhiteMessageLog>> channelID_MessageSentToInitiator;
    private Map<String, List<WhiteMessageLog>> channelID_MessageReceivedFromInitiator;

    private List<ComputedProcessStateBean> computedProcessStateList;

    private static class ComputedProcessStateBean {
        private long currentProcess;
        private long initiatedProcess;
        private boolean isInConsistentState = true;

        private long messageInTransitFromInitiatorToCurrentProcess;
        private long messageInTransitFromCurrentProcessToInitiator;

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

        public void setInTransitFromInitiatorToCurrentProcess(long message) {
            this.messageInTransitFromInitiatorToCurrentProcess = message;
        }

        public long getMessageInTransitFromInitiatorToCurrentProcess() {
            return this.messageInTransitFromInitiatorToCurrentProcess;
        }

        public void setInTransitFromCurrentProcessToInitiator(long message) {
            this.messageInTransitFromCurrentProcessToInitiator = message;
        }

        public long getMessageInTransitFromCurrentProcessToInitiator() {
            return this.messageInTransitFromInitiatorToCurrentProcess;
        }
    }



    public LaiYangSnapshot(long timestamp, Long localState, Long initiatorProcessID, Long totalNoOfProcesses) {
        this.timestamp = timestamp;
        this.date = new Date(new Timestamp(timestamp).getTime());
        this.totalNoOfProcesses = totalNoOfProcesses;
        this.isInProgress = new AtomicBoolean(true);
        this.initiatorProcessID = initiatorProcessID;
        this.recordedLocalState = new AtomicLong(localState);
        this.computedProcessStateList  = new LinkedList<>();

        this.initiator_channelID_MessagesSent = new HashMap<>();
        this.initiator_channelID_MessagesReceived = new HashMap<>();

        this.channelID_MessageSentToInitiator = new HashMap<>();
        this.channelID_MessageReceivedFromInitiator = new HashMap<>();
        this.otherProcessIDToLocalState = new HashMap<>();
    }

    public void setMessagesSentByInitiator(String channelID, List<WhiteMessageLog> messages) {
        this.initiator_channelID_MessagesSent.put(channelID, messages);
    }

    public void setMessageReceivedByInitiator(String channelID, List<WhiteMessageLog> messages) {
        this.initiator_channelID_MessagesReceived.put(channelID, messages);
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

    public Date getDate() {
        return this.date;
    }

    public Long getLocalState() {
        return recordedLocalState.get();
    }

    public void setLocalState(Long localState) {
        recordedLocalState.set(localState);
    }

    private void computeGlobalState() {
        Long initiatedProcessID = this.getInitiatorProcessID();

        for (Map.Entry<Long, Long> entry : otherProcessIDToLocalState.entrySet()) {
            Long currentProcessID = entry.getKey();

            Long messagesSentFromInitiator = 0l;
            Long messagesReceivedByInitiator = 0l;

            Long messagesSentToInitiator = 0l;
            Long messagesReceivedFromInitiator = 0l;


            Iterator<WhiteMessageLog> iterator = this.initiator_channelID_MessagesSent.get(initiatedProcessID+ ":"+currentProcessID).iterator();
            while (iterator.hasNext()) {
                messagesSentFromInitiator += iterator.next().getData();
            }

            iterator = this.initiator_channelID_MessagesReceived.get(currentProcessID+ ":"+initiatedProcessID).iterator();
            while (iterator.hasNext()) {
                messagesReceivedByInitiator += iterator.next().getData();
            }

            iterator = this.channelID_MessageSentToInitiator.get(currentProcessID+ ":"+initiatedProcessID).iterator();
            while (iterator.hasNext()) {
                messagesSentToInitiator += iterator.next().getData();
            }

            iterator = this.channelID_MessageReceivedFromInitiator.get(initiatedProcessID+ ":"+currentProcessID).iterator();
            while (iterator.hasNext()) {
                messagesReceivedFromInitiator += iterator.next().getData();
            }

            Long diffMessagesSentFromInitiator = messagesSentFromInitiator - messagesReceivedFromInitiator;
            Long diffMessagesReceivedByInitiator = messagesSentToInitiator - messagesReceivedByInitiator;


            boolean localConsistentState = diffMessagesSentFromInitiator >= 0;
            localConsistentState = localConsistentState && diffMessagesReceivedByInitiator >= 0;

            this.isInGlobalConsistentState = this.isInGlobalConsistentState && localConsistentState;

            ComputedProcessStateBean bean = new ComputedProcessStateBean();

            bean.setCurrentProcess(currentProcessID);
            bean.setConsistentState(localConsistentState);
            bean.setInTransitFromInitiatorToCurrentProcess(diffMessagesSentFromInitiator);
            bean.setInTransitFromCurrentProcessToInitiator(diffMessagesReceivedByInitiator);

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
        channelID_MessageSentToInitiator.put(msg.getFrom() + ":" + msg.getTo(), msg.getMessageSent());
        channelID_MessageReceivedFromInitiator.put(msg.getTo() + ":" + msg.getFrom(), msg.getMessageReceived());
        if (otherProcessIDToLocalState.size() == totalNoOfProcesses - 1) {
            computeGlobalState();
            setIsInProgress(Boolean.FALSE);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Long initiatedProcessID = this.getInitiatorProcessID();

        builder.append("Snapshot taken at ").append(this.getDate());
        builder.append(Messages.NEWLINE);
        builder.append("Initiated ID : ").append(initiatedProcessID);
        builder.append(Messages.SPACE);
        builder.append("Process State : ").append(this.getLocalState());
        builder.append(Messages.NEWLINE);
        builder.append(Messages.NEWLINE);

        if (isInProgress()) {
            builder.append(Messages.SNAPSHOT_IS_IN_PROGRESS);
            return builder.append(Messages.NEWLINE).toString();
        }

        for (ComputedProcessStateBean bean : computedProcessStateList) {
            Long currentProcessID = bean.getCurrentProcess();
            Long initiatorToProcessDiff = bean.getMessageInTransitFromInitiatorToCurrentProcess();
            Long processToInitiatorDiff = bean.getMessageInTransitFromCurrentProcessToInitiator();

            builder.append("The local state at process ")
                    .append(bean.getCurrentProcess())
                    .append(" is ")
                    .append(otherProcessIDToLocalState.get(bean.getCurrentProcess()));
            builder.append(Messages.NEWLINE);

            if (!bean.getConsistentState()) {
                builder.append("State between process ")
                        .append(initiatedProcessID)
                        .append(" and ")
                        .append(currentProcessID)
                        .append(" is not consistent");
                builder.append(Messages.NEWLINE);
            }

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
            builder.append(Messages.NEWLINE);
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
