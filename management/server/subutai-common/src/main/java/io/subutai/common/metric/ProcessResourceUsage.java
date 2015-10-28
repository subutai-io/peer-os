package io.subutai.common.metric;


import io.subutai.common.peer.ContainerId;


/**
 * Resource usage of a process
 */
public class ProcessResourceUsage
{
    private ContainerId containerId;
    private int pid;
    private double usedCpu;
    private double usedRam;


    public ProcessResourceUsage( final ContainerId containerId, final int pid )
    {
        this.containerId = containerId;
        this.pid = pid;
    }


    public int getPid()
    {
        return pid;
    }


    public ContainerId getContainerId()
    {
        return containerId;
    }


    public double getUsedCpu()
    {
        return usedCpu;
    }


    public double getUsedRam()
    {
        return usedRam;
    }


    public void setUsedCpu( final double usedCpu )
    {
        this.usedCpu = usedCpu;
    }


    public void setUsedRam( final double usedRam )
    {
        this.usedRam = usedRam;
    }


    public void setContainerId( final ContainerId containerId )
    {
        this.containerId = containerId;
    }
}
