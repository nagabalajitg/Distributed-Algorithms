package com.balaji.naga.message;

import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.compute.Process;

public interface Message {
    enum MessageType {
        DATA, SPECIAL
    }

    Long getTo();
    Long getFrom();
    Long getInTransitMessage();
    MessageType getMessageType();
    void processAtSender(Process process, Channel channel);
    void processAtReceiver(Process process, Channel channel);
}
