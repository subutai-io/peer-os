package org.safehaus.subutai.common.metric;


import java.util.UUID;

import com.google.common.base.Objects;


/**
 * Interface for metrics
 *
 * {"host":"py991745969", "totalRam":"16501141504", "availableRam":"15651282944", "usedRam":"849711104",
 * "usedCpu":"22220161270753", "availableDiskRootfs":"293251227648", "availableDiskVar":"1175086145536",
 * "availableDiskHome":"1175086145536", "availableDiskOpt":"293251227648", "usedDiskRootfs":"584933376",
 * "usedDiskVar":"258498560", "usedDiskHome":"258498560", "usedDiskOpt":"584933376", "totalDiskRootfs":"298500227072",
 * "totalDiskVar":"1194000908288", "totalDiskHome":"1194000908288", "totalDiskOpt":"298500227072"}
 */
public abstract class Metric
{

    protected String host;
    protected UUID hostId;
    protected Double totalRam;
    protected Double availableRam;
    protected Double usedRam;
    protected Double usedCpu;

    protected Double availableDiskRootfs;
    protected Double availableDiskVar;
    protected Double availableDiskHome;
    protected Double availableDiskOpt;
    protected Double usedDiskRootfs;
    protected Double usedDiskVar;
    protected Double usedDiskHome;
    protected Double usedDiskOpt;
    protected Double totalDiskRootfs;
    protected Double totalDiskVar;
    protected Double totalDiskHome;
    protected Double totalDiskOpt;


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


    public Double getTotalDiskOpt()
    {
        return totalDiskOpt;
    }


    public Double getAvailableDiskRootfs()
    {
        return availableDiskRootfs;
    }


    public Double getAvailableDiskVar()
    {
        return availableDiskVar;
    }


    public Double getAvailableDiskHome()
    {
        return availableDiskHome;
    }


    public Double getAvailableDiskOpt()
    {
        return availableDiskOpt;
    }


    public Double getUsedDiskRootfs()
    {
        return usedDiskRootfs;
    }


    public Double getUsedDiskVar()
    {
        return usedDiskVar;
    }


    public Double getUsedDiskHome()
    {
        return usedDiskHome;
    }


    public Double getUsedDiskOpt()
    {
        return usedDiskOpt;
    }


    public Double getTotalDiskRootfs()
    {
        return totalDiskRootfs;
    }


    public Double getTotalDiskVar()
    {
        return totalDiskVar;
    }


    public Double getTotalDiskHome()
    {
        return totalDiskHome;
    }


    /**
     * Returns used CPU time in nanoseconds
     */
    public Double getUsedCpu()
    {
        return usedCpu;
    }


    public UUID getHostId()
    {
        return hostId;
    }


    @Override
    public String toString()
    {
        return Objects.toStringHelper( this ).add( "host", host ).add( "hostId", hostId ).add( "totalRam", totalRam )
                      .add( "availableRam", availableRam ).add( "usedRam", usedRam ).add( "usedCpu", usedCpu )
                      .add( "availableDiskRootfs", availableDiskRootfs ).add( "availableDiskVar", availableDiskVar )
                      .add( "availableDiskHome", availableDiskHome ).add( "availableDiskOpt", availableDiskOpt )
                      .add( "usedDiskRootfs", usedDiskRootfs ).add( "usedDiskVar", usedDiskVar )
                      .add( "usedDiskHome", usedDiskHome ).add( "usedDiskOpt", usedDiskOpt )
                      .add( "totalDiskRootfs", totalDiskRootfs ).add( "totalDiskVar", totalDiskVar )
                      .add( "totalDiskHome", totalDiskHome ).add( "totalDiskOpt", totalDiskOpt ).toString();
    }
}
