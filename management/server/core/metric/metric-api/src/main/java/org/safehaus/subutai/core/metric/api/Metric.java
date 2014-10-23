package org.safehaus.subutai.core.metric.api;


/**
 * Interface for metrics
 *
 * {"host":"test", "totalRam":"16306260","availableRam":"195028", "usedRam":"16111232", "cpuLoad5":"5.7",
 * "availableDisk" : "123", "usedDisk" : "123", "totalDisk" : "123"}
 */
public interface Metric
{

    public String getHostname();

    public Double getAvailableRam();

    public Double getUsedRam();

    public Double getTotalRam();

    public Double getAvailableDisk();

    public Double getUsedDisk();

    public Double getTotalDisk();

    public Double getCpuLoad5();
}
