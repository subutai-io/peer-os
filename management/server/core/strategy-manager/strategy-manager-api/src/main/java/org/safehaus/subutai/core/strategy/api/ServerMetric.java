/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.strategy.api;


/**
 * Class which contains current metrics of resource host
 */
public class ServerMetric
{
    private int freeHddMb;
    private int freeRamMb;
    private int cpuLoadPercent;
    private int numOfProcessors;
    private String hostname;


    public ServerMetric( String hostname, int freeHddMb, int freeRamMb, int cpuLoadPercent, int numOfProcessors )
    {
        this.hostname = hostname;
        this.freeHddMb = freeHddMb;
        this.freeRamMb = freeRamMb;
        this.cpuLoadPercent = cpuLoadPercent;
        this.numOfProcessors = numOfProcessors;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }


    public int getNumOfProcessors()
    {
        return numOfProcessors;
    }


    public int getFreeHddMb()
    {
        return freeHddMb;
    }


    public int getFreeRamMb()
    {
        return freeRamMb;
    }


    public int getCpuLoadPercent()
    {
        return cpuLoadPercent;
    }


    @Override
    public String toString()
    {
        return "ServerMetric{" + "freeHddMb=" + freeHddMb + ", freeRamMb=" + freeRamMb + ", cpuLoadPercent="
                + cpuLoadPercent + ", numOfProcessors=" + numOfProcessors + '}';
    }
}
