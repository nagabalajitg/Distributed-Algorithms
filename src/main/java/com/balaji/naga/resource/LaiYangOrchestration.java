package com.balaji.naga.resource;

import com.balaji.naga.resource.compute.LaiYangProcess;
import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.snapshot.Snapshot;
import com.balaji.naga.utils.Messages;

import java.util.logging.Logger;

public class LaiYangOrchestration extends Orchestration {
    private static final Logger LOGGER = Logger.getLogger(LaiYangOrchestration.class.getName());

    public LaiYangOrchestration() {
        super();
    }

    protected Process createAProcess(long processID, long initialValue) {
        return new LaiYangProcess(processID, initialValue);
    }

    public void initiateGlobalSnapshotAt(long processID, long data) {
        LaiYangProcess process = (LaiYangProcess) this.getProcess(processID);
        process.snapshotGlobalState();
    }

    public Snapshot getSnapshotOfProcess(Long processID){
        LaiYangProcess process = (LaiYangProcess) this.getProcess(processID);
        if (process == null) {
            throw new IllegalArgumentException(Messages.INVALID_PROCESS_ID);
        }

        return process.getLastSnapshot();
    }
}
