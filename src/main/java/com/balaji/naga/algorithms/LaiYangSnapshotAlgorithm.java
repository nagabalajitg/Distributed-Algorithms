package com.balaji.naga.algorithms;

import java.util.Iterator;

public interface LaiYang extends SnapshotAlgorithms {
    enum MessageColor {
        RED, WHITE
    }

    class History {
        private long time;
        private long channelID;

        public History (long time, long channelID) {
            this.time = time;
            this.channelID = channelID;
        }

        public long getTime() {
            return this.time;
        }

        public long getChannelID() {
            return this.channelID;
        }
    }

    Iterator<History> getLog();
}
