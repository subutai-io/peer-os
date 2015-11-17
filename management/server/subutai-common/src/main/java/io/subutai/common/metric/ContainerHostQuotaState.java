package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Container host metrics
 */
public class ContainerHostQuotaState extends BaseMetric
{
    @Expose
    @SerializedName( "container" )
    @JsonProperty( "container" )
    protected String hostName;
    @Expose
    @SerializedName( "ram" )
    @JsonProperty( "ram" )
    protected int ram;
    @Expose
    @SerializedName( "cpu" )
    @JsonProperty( "cpu" )
    protected int cpu;
    @Expose
    @SerializedName( "Disk" )
    @JsonProperty( "Disk" )
    protected DiskQuota disk;


    public DiskQuota getDisk()
    {
        return disk;
    }


    public int getCpu()
    {
        return cpu;
    }


    public int getRam()
    {
        return ram;
    }


    public String getHostName()
    {
        return hostName;
    }


    @Override
    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
    }
}
