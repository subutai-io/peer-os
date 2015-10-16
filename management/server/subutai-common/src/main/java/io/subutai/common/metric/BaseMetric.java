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
public class BaseMetric
{
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


    public BaseMetric()
    {
    }


    public BaseMetric( final Ram ram )
    {
        this.ram = ram;
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
        if ( disk != null )
        {
            return disk.getAvailableSpace();
        }
        else
        {
            return 0.0;
        }
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
}
