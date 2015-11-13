package io.subutai.common.metric;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


/**
 * Base class for host metrics
 */
@XmlRootElement
@XmlAccessorType( XmlAccessType.FIELD )
public class HostMetric
{
    @JsonIgnore
    private String peerId;

    @JsonIgnore
    protected String hostId;
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


    public HostMetric()
    {
    }


    public HostMetric( final Ram ram )
    {
        this.ram = ram;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
    }


    public String getHostName()
    {
        return hostName;
    }


    public void setHostName( final String hostName )
    {
        this.hostName = hostName;
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
        return ram != null ? ram.getTotal() : 0;
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


    public Double getFreeRam()
    {
        return ram != null ? ram.free : 0;
    }


    public Double getTotalSpace()
    {
        return disk != null ? disk.getTotal() : 0;
    }
}
