package com.balaji.naga.message;

import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm;
import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog.MessageBoundType;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.resource.compute.LaiYangProcess;
import com.balaji.naga.resource.compute.Process;
import com.balaji.naga.snapshot.LaiYangSnapshot;

import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

public class LaiYangMessage extends DataMessage implements LaiYangSnapshotAlgorithm {
    private Long timestamp;
    private MessageColor messageColor;

    private Long processLocalState;
    private List<WhiteMessageLog> sentMessageLog;
    private List<WhiteMessageLog> receivedMessageLog;

    private static final Message.MessageType MESSAGE_TYPE = Message.MessageType.SPECIAL;

    public LaiYangMessage(MessageColor messageColor, Long data, Long from, Long to) {
        super(data, from, to);
        this.messageColor = messageColor;
        this.timestamp = System.currentTimeMillis();
    }

    public void setProcessLocalState(Long processLocalState) {
        this.processLocalState = processLocalState;
    }

    public Long getProcessLocalState() {
        return this.processLocalState;
    }

    public void setMessageSent(List<WhiteMessageLog> sentMessages) {
        this.sentMessageLog = sentMessages;
    }

    public List<WhiteMessageLog> getMessageSent() {
        return this.sentMessageLog;
    }

    public void setMessageReceived(List<WhiteMessageLog> receivedMessages) {
        this.receivedMessageLog = receivedMessages;
    }

    public List<WhiteMessageLog> getMessageReceived() {
        return  this.receivedMessageLog;
    }

    public MessageColor getMessageColor() {
        return this.messageColor;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    @Override
    public MessageType getMessageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public void processAtSender(Process process, Channel channel) {
        if (getMessageColor() == MessageColor.WHITE) {
            ((LaiYangProcess)process).log(
                                        channel.getChannelID(),
                                        MessageBoundType.OUTBOUND,
                                        process.getProcessID(),
                                        this.getInTransitMessage(),
                                        System.currentTimeMillis()
                                    );
        }
        super.processAtSender(process, channel);
    }

    private void processRedMessage(LaiYangProcess process, Channel channel) {
        if (process.getProcessColor() == MessageColor.RED) {
            LaiYangSnapshot snapshot = process.getLastSnapshot();
            if (snapshot != null && snapshot.isInProgress()) {
                snapshot.processMessage(this);
                if (!snapshot.isInProgress()) {
                    process.setProcessColor(MessageColor.WHITE);
                }
            }
        } else {
            List<WhiteMessageLog> sentMessages = new LinkedList<>();
            List<WhiteMessageLog> receivedMessages = new LinkedList<>();

            Long localState = process.getLocalState();
            Long currentTime = System.currentTimeMillis();
            LaiYangSnapshot snapshot = process.hasSnapshot() ? process.getLastSnapshot() : null;
            Long lastSnapshotTime = snapshot != null ? snapshot.getTimestamp() : 0;

            WhiteMessageLog to = new WhiteMessageLog(MessageBoundType.INBOUND, -1l, -1l, currentTime);
            WhiteMessageLog from = new WhiteMessageLog(MessageBoundType.INBOUND, -1l, -1l, lastSnapshotTime);

            TreeSet<WhiteMessageLog> whiteMessagesSent = (TreeSet<WhiteMessageLog> )process.getWhiteMessagesForChannel(channel.getProcess2().getProcessID() + ":"+channel.getProcess1().getProcessID());
            TreeSet<WhiteMessageLog> whiteMessagesReceived = (TreeSet<WhiteMessageLog> ) process.getWhiteMessagesForChannel(channel.getChannelID());

            if (whiteMessagesSent != null) {
                for (WhiteMessageLog log : whiteMessagesSent.subSet(from, to)) {
                    sentMessages.add(log);
                }
            }

            if (whiteMessagesReceived != null) {
                for (WhiteMessageLog log : whiteMessagesReceived.subSet(from, to)) {
                    receivedMessages.add(log);
                }
            }

            LaiYangMessage message = new LaiYangMessage(MessageColor.RED, 0l, process.getProcessID(), channel.getProcess1().getProcessID());

            message.setMessageSent(sentMessages);
            message.setMessageReceived(receivedMessages);
            message.setProcessLocalState(localState);

            process.sendMessage(message);
        }
    }

    public void processAtReceiver(Process process, Channel channel) {
        LaiYangProcess LYProcess = (LaiYangProcess)process;
        if (getMessageColor() == MessageColor.RED) {
            this.processRedMessage(LYProcess, channel);
        } else {
            LYProcess.log(
                    channel.getChannelID(),
                    MessageBoundType.INBOUND,
                    process.getProcessID(),
                    this.getInTransitMessage(),
                    System.currentTimeMillis()
            );
        }
        super.processAtReceiver(process, channel);
    }
}
