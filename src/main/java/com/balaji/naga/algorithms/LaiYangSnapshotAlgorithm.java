package com.balaji.naga.algorithms;

public interface LaiYangSnapshotAlgorithm extends SnapshotAlgorithms {
    enum MessageColor {
        RED, WHITE
    }

    class WhiteMessageLog {
        public enum MessageBoundType {
            INBOUND, OUTBOUND
        }

        private Long data;
        private Long timestamp;
        private Long processID;
        private MessageBoundType boundType;

        public WhiteMessageLog(MessageBoundType boundType, Long data, Long processID, Long timestamp) {
            this.data = data;
            this.boundType = boundType;
            this.timestamp = timestamp;
            this.processID = processID;
        }

        public Long getData() { return this.data; }
        public Long getTimestamp() {
            return this.timestamp;
        }
    }
}
