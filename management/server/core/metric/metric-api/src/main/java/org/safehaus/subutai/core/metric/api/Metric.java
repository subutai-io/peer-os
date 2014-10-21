package org.safehaus.subutai.core.metric.api;


/**
 * Interface for metrics
 */
public interface Metric
{
    public Double getAvailableRAM();

    public Double getTotalRAM();

    public Double getAvailableDisk();

    public Double getTotalDisk();

    public Double getUsedCPU();

    public Double getUnusedCPU();

    public Double getReadDiskIO();

    public Double getWriteDiskIO();

    public Double getInNetIO();

    public Double getOutNetIO();
}
