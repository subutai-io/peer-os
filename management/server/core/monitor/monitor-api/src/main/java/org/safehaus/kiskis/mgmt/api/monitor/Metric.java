package org.safehaus.kiskis.mgmt.api.monitor;

public enum Metric {

    CPU_USER("%"),
    CPU_SYSTEM("%"),
    CPU_IDLE("%"),
    CPU_WIO("%"),
    MEM_FREE("KB"),
    MEM_CACHED("KB"),
    MEM_BUFFERS("KB"),
    SWAP_FREE("KB"),
    PKTS_IN("packets/sec"),
    PKTS_OUT("packets/sec"),
    BYTES_IN("bytes/sec"),
    BYTES_OUT("bytes/sec"),
    PART_MAX_USED("%");

    private String unit = "";

    private Metric(String unit) {
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }
}
