package com.balaji.naga.algorithms;

import com.balaji.naga.message.Message;
import com.balaji.naga.message.DataMessage;
import com.balaji.naga.resource.compute.Process;

import java.util.Iterator;
import java.util.List;

public class LaiYangMessage extends DataMessage implements LaiYangSnapshotAlgorithm  {
    private Long snapshot;
    private List<History> log;
    private MessageColor messageColor;
    private static final Message.MessageType MESSAGE_TYPE = Message.MessageType.Special;

    public LaiYangMessage(MessageColor messageColor, Long data, Long from, Long to) {
        super(data, from, to);
        this.messageColor = messageColor;
    }

    public MessageColor getMessageColor() {
        return this.messageColor;
    }

    @Override
    public MessageType getMessageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public Long getSnapshot() {
        return this.snapshot;
    }

    @Override
    public void takeSnapshot(Process process) {
         this.snapshot = process.getData();
    }

    @Override
    public void processAtSender(Process process) {
        if (getMessageColor() == MessageColor.RED) {
            this.takeSnapshot(process);
        }
        process.updateData(-getInTransitMessage());
    }

    @Override
    public void processAtReceiver(Process process) {
        if (getMessageColor() == MessageColor.RED) {
            this.takeSnapshot(process);
        }
        process.updateData(getInTransitMessage());
    }

    @Override
    public Iterator<History> getLog() {
        return this.log.iterator();
    }
}
