package com.balaji.naga.resource.compute;

import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.MessageColor;
import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog;
import com.balaji.naga.algorithms.LaiYangSnapshotAlgorithm.WhiteMessageLog.MessageBoundType;
import com.balaji.naga.algorithms.SnapshotAlgorithms;
import com.balaji.naga.message.LaiYangMessage;
import com.balaji.naga.resource.communication.Channel;
import com.balaji.naga.snapshot.LaiYangSnapshot;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LaiYangProcess extends Process implements SnapshotAlgorithms {

    private final Map<String, SortedSet<WhiteMessageLog>> channelIDToWhiteMessage = new ConcurrentHashMap<>();
    private MessageColor messageColor = MessageColor.WHITE;
    private SortedSet<LaiYangSnapshot> laiYangSnapshots = new TreeSet<>(new Comparator<LaiYangSnapshot>() {
        @Override
        public int compare(LaiYangSnapshot s1, LaiYangSnapshot s2) {
            Long diff = s1.getTimestamp() - s2.getTimestamp();
            return diff > 0 ? 1 : diff == 0 ? 0 : -1;
        }
    });

    public LaiYangProcess(long id, Long data) {
        super(id, data);
    }

    public void setProcessColor(MessageColor color) {
        synchronized (this) {
            this.messageColor = color;
        }
    }

    public MessageColor getProcessColor() {
        synchronized (this) {
            return this.messageColor;
        }
    }

    public void snapshotGlobalState() {
        long timestamp = System.currentTimeMillis();
        LaiYangSnapshot lastSnapshot = getLastSnapshot();
        int totalNoOfProcesses = getAllReadChannels().size() + 1;

        if(lastSnapshot == null || !lastSnapshot.isInProgress()) {
            LaiYangSnapshot newSnapshot = new LaiYangSnapshot(timestamp, this.getLocalState(), this.getProcessID(), (long)totalNoOfProcesses);

            Long lastSnapshotTime = lastSnapshot != null ? lastSnapshot.getTimestamp() : 0;

            WhiteMessageLog to = new WhiteMessageLog(MessageBoundType.INBOUND, -1l, -1l, timestamp);
            WhiteMessageLog from = new WhiteMessageLog(MessageBoundType.INBOUND, -1l, -1l, lastSnapshotTime);

            laiYangSnapshots.add(newSnapshot);
            setProcessColor(MessageColor.RED);

            for (Channel channel : getAllWriteChannels()) {
                List<WhiteMessageLog> messagesSent = new LinkedList<>();
                SortedSet<WhiteMessageLog> whiteMessages = getWhiteMessagesForChannel(channel.getChannelID());
                if (whiteMessages != null) {
                    for (WhiteMessageLog log : whiteMessages.subSet(from, to)) {
                        messagesSent.add(log);
                    }
                }
                newSnapshot.setMessagesSentByInitiator(channel.getChannelID(), messagesSent);
                super.sendMessage(new LaiYangMessage(MessageColor.RED, 0l, this.getProcessID(), channel.getProcess2().getProcessID()));
            }

            for (Channel channel : getAllReadChannels()) {
                List<WhiteMessageLog> messagesReceived = new LinkedList<>();
                SortedSet<WhiteMessageLog> whiteMessages = getWhiteMessagesForChannel(channel.getChannelID());
                if (whiteMessages != null) {
                    for (WhiteMessageLog log : whiteMessages.subSet(from, to)) {
                        messagesReceived.add(log);
                    }
                }
                newSnapshot.setMessageReceivedByInitiator(channel.getChannelID(), messagesReceived);
            }
        }
    }

    public void log (String channelID, MessageBoundType boundType, Long processID, Long data, Long timestamp) {
        SortedSet<WhiteMessageLog> set = channelIDToWhiteMessage.getOrDefault(channelID, new TreeSet<>(new Comparator<WhiteMessageLog>() {
            @Override
            public int compare(WhiteMessageLog m1, WhiteMessageLog m2) {
                Long diff = m1.getTimestamp() - m2.getTimestamp();
                return diff > 0 ? 1 : diff == 0 ? 0 : -1;
            }
        }));
        set.add(new WhiteMessageLog(boundType, data, processID , timestamp));
        channelIDToWhiteMessage.put(channelID, set);
    }

    public SortedSet<LaiYangSnapshot> getAllSnapshots() {
        return this.laiYangSnapshots;
    }

    public boolean hasSnapshot() {
        return getAllSnapshots().size() > 0;
    }

    public LaiYangSnapshot getLastSnapshot() {
        return laiYangSnapshots.size() > 0 ? laiYangSnapshots.last(): null;
    }

    public SortedSet<WhiteMessageLog> getWhiteMessagesForChannel(String channelID) {
        return channelIDToWhiteMessage.get(channelID);
    }
}
