package com.balaji.naga.resource;

import java.util.HashSet;
import java.util.Set;

public class Site implements Resource {
    private long id;
    private Group group;
    private Set<Process> processes;

    private void initProcesses(Set<Long> processIDs) {
        processes = new HashSet<>();
        for (Long processID : processIDs) {
            processes.add(new Process(processID, this));
        }
    }
    public Site (Group group, Set<Long> processIDs) {
        this.group = group;
        this.id = System.currentTimeMillis();
        initProcesses(processIDs);
    }

    public Site (long id, Group group) {
        this.id = id;
        this.group = group;
    }

    public Site (long id, Group group, Set<Long> processIDs) {
        this.id = id;
        this.group = group;
        initProcesses(processIDs);
    }

    public long getSiteID() {
        return this.id;
    }

    public void createProcessesByID(Set<Long> processIDs) {
        for (Long processID : processIDs) {
            this.createProcessByID(processID);
        }
    }

    public void createProcessByID(long processID) {
        processes.add(new Process(processID, this));
    }
}