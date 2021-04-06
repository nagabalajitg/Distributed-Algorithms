package com.balaji.naga.resource.communication;


import com.balaji.naga.message.Message;
import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.utils.Messages;

public interface Channel {
    enum ChannelType {
        FIFO,
        NON_FIF0
    }

    static Channel createChannel(ChannelType type, Process process_1, Process process_2) {
        Channel channel;
        switch (type) {
            case FIFO:
                channel = new FifoChannel(process_1, process_2);
                break;
            case NON_FIF0:
                channel = new NonFifoChannel(process_1, process_2);
                break;
            default:
                throw new IllegalArgumentException(Messages.INVALID_CHANNEL_TYPE);
        }
        return channel;
    }

    Process getProcess1();
    Process getProcess2();

    String getChannelID();

    boolean hasMessage();
    Message readMessage();
    void sendMessage(Message message);
}