package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

public enum Metric {

    MEMORY {
        @Override
        public String getTitleY() {
            return "KB";
        }
    },

    CPU {
        @Override
        public String getTitleY() {
            return "%";
        }
    },

    DISK {
        @Override
        public String getTitleY() {
            return "read + write";
        }
    },

    NETWORK {
        @Override
        public String getTitleY() {
            return "rx + tx";
        }
    };

    public abstract String getTitleY();
}
