package com.balaji.naga.snapshot;


import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Snapshot {
    private Long timestamp;
    private Long initiatorProcessID;
    private Map<Long, Long> otherProcessIDToLocalState;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageSent;
    private Map<String, List<WhiteMessageLog>> channelIDToMessageReceived;
    private AtomicLong recordedLocalState;
    private final AtomicBoolean isInProgress;
    
    public Snapshot(long timestamp, Long localState, Long initiatorProcessID) {
        this.timestamp = timestamp;
        this.isInProgress = new AtomicBoolean(true);
        this.initiatorProcessID = initiatorProcessID;
        this.recordedLocalState = new AtomicLong(localState);
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

    public Long getTimestamp() {
        return this.timestamp;
    }

    public Long getLocalState() {
        return recordedLocalState.get();
    }

    public void setLocalState(Long localState) {
        recordedLocalState.set(localState);
    }

}
