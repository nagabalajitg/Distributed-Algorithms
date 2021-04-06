package com.balaji.naga.message;

import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.compute.Process;

public class DataMessage implements Message {
    private Long to;
    private Long from;
    private Long inTransit;
    private static final MessageType MESSAGE_TYPE = MessageType.DATA;

    public DataMessage(Long data, Long from, Long to) {
        this.to = to;
        this.from = from;
        this.inTransit = data;
    }

    @Override
    public MessageType getMessageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public Long getTo() {
        return this.to;
    }

    @Override
    public Long getFrom() {
        return this.from;
    }

    @Override
    public Long getInTransitMessage() {
        return this.inTransit;
    }

    @Override
    public void processAtSender(Process process, Channel channel) {
        process.updateData(-inTransit);
        channel.sendMessage(this);
    }

    @Override
    public void processAtReceiver(Process process, Channel channel) {
        process.updateData(inTransit);
    }
}
