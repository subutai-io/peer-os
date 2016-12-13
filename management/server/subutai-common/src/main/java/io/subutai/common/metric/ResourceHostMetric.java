package io.subutai.common.metric;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.subutai.common.host.InstanceType;
import io.subutai.common.host.ResourceHostInfoModel;


/**
 * Resource host metric class
 */
public class ResourceHostMetric extends BaseMetric
{
    @Expose
    @JsonProperty
    private InstanceType instanceType;

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

    @Expose
    @JsonProperty
    private Integer containersCount;

    @Expose
    @JsonProperty
    private boolean management;

    @Expose
    @JsonProperty
    private Date createdTime = new Date();


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
        this.instanceType = hostInfo.getInstanceType();
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
    public Double getUsedSpace()
    {
        return disk != null ? disk.getUsed() : 0;
    }


    @JsonIgnore
    public Double getTotalRam()
    {
        return ram != null && ram.total != null ? ram.total : 0;
    }


    @JsonIgnore
    public Double getAvailableRam()
    {
        return ram != null && ram.free != null ? ram.free : 0;
    }


    @JsonIgnore
    public Double getUsedCpu()
    {

        return cpu != null ? 100 - cpu.idle : null;
    }

    @JsonIgnore
    public Double getCpuIdle()
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
    public Double getTotalSpace()
    {
        return disk != null && disk.total != null ? disk.total : 0;
    }


    @JsonIgnore
    public Integer getContainersCount()
    {
        return containersCount;
    }


    @JsonIgnore
    public InstanceType getInstanceType()
    {
        return instanceType;
    }


    @JsonIgnore
    public double getCpuFrequency()
    {
        return cpu.getFrequency();
    }


    public Date getCreatedTime()
    {
        return createdTime;
    }


    public void setCreatedTime( final Date createdTime )
    {
        this.createdTime = createdTime;
    }


    public void updateMetrics( final ResourceHostMetric resourceHostMetric )
    {
        this.hostName = resourceHostMetric.hostName;
        this.cpu = resourceHostMetric.cpu;
        this.ram = new Ram( resourceHostMetric.ram.total != null ? resourceHostMetric.ram.total : 0.0,
                resourceHostMetric.ram.free != null ? resourceHostMetric.ram.free : 0.0 );

        this.disk = new Disk( resourceHostMetric.ram.total != null ? resourceHostMetric.ram.total : 0.0,
                resourceHostMetric.disk.used != null ? resourceHostMetric.disk.used : 0.0 );
        this.disk = resourceHostMetric.disk;
    }


    @Override
    public String toString()
    {
        return String
                .format( "%s %s, CPU used:%f, Free ram: %f, Available disk space: %f", super.toString(), getCpuModel(),
                        getUsedCpu(), getAvailableRam(), getAvailableSpace() );
    }


    public void setManagement( final boolean management )
    {
        this.management = management;
    }


    public boolean isManagement()
    {
        return management;
    }
}
