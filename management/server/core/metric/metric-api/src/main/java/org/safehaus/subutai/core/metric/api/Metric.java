package org.safehaus.subutai.core.metric.api;


/**
 * Interface for metrics
 *
 * {"host":"test", "totalRam":"16306260","availableRam":"195028", "usedRam":"16111232", "usedCpu":"123", "availableDisk"
 * : "123", "usedDisk" : "123", "totalDisk" : "123"}
 */
public abstract class Metric
{

    protected String host;
    protected Double availableRam;
    protected Double usedRam;
    protected Double totalRam;
    protected Double availableDisk;
    protected Double usedDisk;
    protected Double totalDisk;
    protected Double usedCpu;


    /**
     * Returns source host name
     */
    public String getHost()
    {
        return host;
    }


    /**
     * Returns available ram in bytes
     */
    public Double getAvailableRam()
    {
        return availableRam;
    }


    /**
     * Returns used ram in bytes
     */
    public Double getUsedRam()
    {
        return usedRam;
    }


    /**
     * Returns total ram in bytes
     */
    public Double getTotalRam()
    {
        return totalRam;
    }


    /**
     * Returns available disk in bytes
     */
    public Double getAvailableDisk()
    {
        return availableDisk;
    }


    /**
     * Returns used disk in bytes
     */
    public Double getUsedDisk()
    {
        return usedDisk;
    }


    /**
     * Returns total disk in bytes
     */
    public Double getTotalDisk()
    {
        return totalDisk;
    }


    /**
     * Returns used CPU time in nanoseconds
     */
    public Double getUsedCpu()
    {
        return usedCpu;
    }
}
