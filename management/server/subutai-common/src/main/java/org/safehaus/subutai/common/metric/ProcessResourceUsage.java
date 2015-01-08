package org.safehaus.subutai.common.metric;


/**
 * Resource usage of a process
 */
public class ProcessResourceUsage
{
    private String host;
    private int usedCpu;
    private int usedRam;


    public String getHost()
    {
        return host;
    }


    public int getUsedCpu()
    {
        return usedCpu;
    }


    public int getUsedRam()
    {
        return usedRam;
    }
}
