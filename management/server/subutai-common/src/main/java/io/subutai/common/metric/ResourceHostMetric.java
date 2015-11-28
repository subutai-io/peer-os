package io.subutai.common.metric;


import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;


/**
 * Resource host metric class
 */
public class ResourceHostMetric extends BaseMetric
{
    @Expose
    @SerializedName( "host" )
    @JsonProperty( "host" )
    protected String hostName;
    @Expose
    @SerializedName( "RAM" )
    @JsonProperty( "RAM" )
    protected Ram ram;
    @Expose
    @SerializedName( "CPU" )
    @JsonProperty( "CPU" )
    protected Cpu cpu;
    @Expose
    @SerializedName( "Disk" )
    @JsonProperty( "Disk" )
    protected Disk disk;
    @JsonProperty
    private Integer containersCount;


    public ResourceHostMetric()
    {
    }


    public ResourceHostMetric( final String peerId )
    {
        this.peerId = peerId;
    }


    public ResourceHostMetric( final String peerId, final ResourceHostInfoModel hostInfo )
    {
        super( peerId, hostInfo );
        this.containersCount = hostInfo.getContainers().size();
    }


    public String getHostName()
    {
        return hostName;
    }


    @JsonIgnore
    public Double getAvailableSpace()
    {
        return disk != null ? disk.getAvailableSpace() : 0;
    }


    @JsonIgnore
    public Double getTotalRam()
    {
        return ram != null ? ram.getTotal() : 0;
    }


    @JsonIgnore
    public Double getAvailableRam()
    {
        return ram != null ? ram.getFree() : 0;
    }


    @JsonIgnore
    public Double getUsedCpu()
    {

        return cpu != null ? cpu.idle : null;
    }


    @JsonIgnore
    public String getCpuModel()
    {
        return cpu != null ? cpu.model : null;
    }


    @JsonIgnore
    public int getCpuCore()
    {
        return cpu != null ? cpu.coreCount : 0;
    }


    @JsonIgnore
    public Double getFreeRam()
    {
        return ram != null ? ram.free : 0;
    }


    @JsonIgnore
    public Double getTotalSpace()
    {
        return disk != null ? disk.getTotal() : 0;
    }


    @JsonIgnore
    public Integer getContainersCount()
    {
        return containersCount;
    }


    public void updateMetrics( final ResourceHostMetric resourceHostMetric )
    {
        this.hostName = resourceHostMetric.hostName;
        this.cpu = resourceHostMetric.cpu;
        this.ram = resourceHostMetric.ram;
        this.disk = resourceHostMetric.disk;
    }


    @Override
    public String toString()
    {
        return String.format( "%s %s", getPeerId() != null ? getPeerId() : "UNKNOWN", super.toString() );
    }
}
