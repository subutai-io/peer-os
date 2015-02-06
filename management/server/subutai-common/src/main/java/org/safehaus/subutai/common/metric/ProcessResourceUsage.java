package org.safehaus.subutai.common.metric;


/**
 * Resource usage of a process
 */
public class ProcessResourceUsage
{
    private String host;
    private double usedCpu;
    private double usedRam;


    public String getHost()
    {
        return host;
    }


    public double getUsedCpu()
    {
        return usedCpu;
    }


    public double getUsedRam()
    {
        return usedRam;
    }
}
