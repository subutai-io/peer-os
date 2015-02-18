package org.safehaus.subutai.common.metric;


/**
 * Total local peer resource usage of owner
 */
public final class OwnerResourceUsage
{
    //total used ram in MB
    private double usedRam;

    //total used CPU in nanoseconds
    private double usedCpu;

    //total used disk (partition rootfs)
    protected Double usedDiskRootfs;

    //total used disk (partition var)
    protected Double usedDiskVar;

    //total used disk (partition home)
    protected Double usedDiskHome;

    //total used disk (partition opt)
    protected Double usedDiskOpt;


    public OwnerResourceUsage( final double usedRam, final double usedCpu, final Double usedDiskRootfs,
                               final Double usedDiskVar, final Double usedDiskHome, final Double usedDiskOpt )
    {
        this.usedRam = usedRam;
        this.usedCpu = usedCpu;
        this.usedDiskRootfs = usedDiskRootfs;
        this.usedDiskVar = usedDiskVar;
        this.usedDiskHome = usedDiskHome;
        this.usedDiskOpt = usedDiskOpt;
    }


    public double getUsedRam()
    {
        return usedRam;
    }


    public double getUsedCpu()
    {
        return usedCpu;
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
}
