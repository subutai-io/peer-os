package org.safehaus.subutai.core.metric.api;


import com.google.common.base.Objects;


/**
 * Interface for metrics
 *
 * {"host":"test", "totalRam":"16306260","availableRam":"195028", "usedRam":"16111232", "cpuLoad5":"5.7",
 * "availableDisk" : "123", "usedDisk" : "123", "totalDisk" : "123"}
 */
public abstract class Metric
{

    protected String hostname;
    protected Double availableRam;
    protected Double usedRam;
    protected Double totalRam;
    protected Double availableDisk;
    protected Double usedDisk;
    protected Double totalDisk;
    protected Double cpuLoad5;


    public String getHostname()
    {
        return hostname;
    }


    public Double getAvailableRam()
    {
        return availableRam;
    }


    public Double getUsedRam()
    {
        return usedRam;
    }


    public Double getTotalRam()
    {
        return totalRam;
    }


    public Double getAvailableDisk()
    {
        return availableDisk;
    }


    public Double getUsedDisk()
    {
        return usedDisk;
    }


    public Double getTotalDisk()
    {
        return totalDisk;
    }


    public Double getCpuLoad5()
    {
        return cpuLoad5;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "hostname", hostname ).add( "availableRam", availableRam )
                      .add( "usedRam", usedRam ).add( "totalRam", totalRam ).add( "availableDisk", availableDisk )
                      .add( "usedDisk", usedDisk ).add( "totalDisk", totalDisk ).add( "cpuLoad5", cpuLoad5 ).toString();
    }
}
