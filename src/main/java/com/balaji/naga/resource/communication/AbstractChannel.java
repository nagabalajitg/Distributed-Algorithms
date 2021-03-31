package com.balaji.naga.communication;

import com.balaji.naga.compute.Process;

public abstract class AbstractChannel implements Channel{
    private Process process1;
    private Process process2;
    AbstractChannel (Process process1, Process process2) {
        this.process1 = process1;
        this.process2 = process2;
    }

}
