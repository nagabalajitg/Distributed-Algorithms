package com.balaji.naga.snapshot;

import com.balaji.naga.message.Message;

public interface Snapshot {
    int totalNoOfRecordedProcess();
    void processMessage(Message message);
}
