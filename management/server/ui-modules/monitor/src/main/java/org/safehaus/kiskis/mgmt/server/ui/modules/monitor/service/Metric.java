package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service;

public enum Metric {

    cpu_user("cpu_user", "%"),
    cpu_system("cpu_system", "%"),
    cpu_idle("cpu_idle", "%"),
    cpu_wio("cpu_wio", "%"),
    mem_free("mem_free", "KB"),
    mem_cached("mem_cached", "KB"),
    mem_buffers("mem_buffers", "KB"),
    swap_free("swap_free", "KB"),
    pkts_in("pkts_in", "packets/sec"),
    pkts_out("pkts_out", "packets/sec"),
    bytes_in("bytes_in", "bytes/sec"),
    bytes_out("bytes_out", "bytes/sec");

    private String text = "";
    private String unit = "";

    private Metric(String text, String unit) {
        this.text = text;
        this.unit = unit;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return text;
    }
}
