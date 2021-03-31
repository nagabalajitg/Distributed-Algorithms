package com.balaji.naga.snapshot;


import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog;
import com.balaji.naga.message.LaiYangMessage;
import com.balaji.naga.message.Message;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class LaiYangSnapshot implements Snapshot {
    private final AtomicBoolean isInProgress;

    private Long timestamp;
    private Long initiatorProcessID;
    private AtomicLong recordedLocalState;
    private Map<String, List<WhiteMessageLog>> messagesSentAtInitiator;
    private Map<String, List<WhiteMessageLog>> messagesReceivedAtInitiator;

    private Map<Long, Long> otherProcessIDToLocalState;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageSent;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageReceived;



    public LaiYangSnapshot(long timestamp, Long localState, Long initiatorProcessID) {
        this.timestamp = timestamp;
        this.isInProgress = new AtomicBoolean(true);
        this.initiatorProcessID = initiatorProcessID;
        this.recordedLocalState = new AtomicLong(localState);

        this.messagesSentAtInitiator = new ConcurrentHashMap<>();
        this.messagesReceivedAtInitiator = new ConcurrentHashMap<>();

        this.channelIDToMessageSent = new ConcurrentHashMap<>();
        this.channelIDToMessageReceived = new ConcurrentHashMap<>();
        this.otherProcessIDToLocalState = new ConcurrentHashMap<>();
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
}
