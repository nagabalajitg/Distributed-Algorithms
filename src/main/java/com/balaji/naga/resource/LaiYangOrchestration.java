package com.balaji.naga.resource;

import com.balaji.naga.resource.compute.LaiYangProcess;
import com.balaji.naga.snapshot.Snapshot;

import java.util.logging.Logger;

public class LaiYangOrchestration extends Orchestration {
    private static final Logger LOGGER = Logger.getLogger(LaiYangOrchestration.class.getName());

    public LaiYangOrchestration() {
        super();
    }

    public void initiateGlobalSnapshotAt(long processID, long data) {
        LaiYangProcess process = (LaiYangProcess) this.getProcess(processID);
        process.snapshotGlobalState();
    }

    public Snapshot getSnapshotOfProcess(Long processID){
        return ((LaiYangProcess) this.getProcess(processID)).getAllSnapshots().last();
    }
}
