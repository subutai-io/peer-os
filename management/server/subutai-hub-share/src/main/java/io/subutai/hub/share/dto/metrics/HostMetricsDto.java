package io.subutai.hub.share.dto.metrics;


import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class HostMetricsDto
{
    public static final String ROOT_PARTITION = "/";
    public static final String MNT_PARTITION = "/mnt";
    public static final List<String> RESOURCE_HOST_PARTITIONS =
            Collections.unmodifiableList( Arrays.asList( ROOT_PARTITION, MNT_PARTITION ) );

    public static final String WAN_INTERFACE = "wan";
    public static final List<String> RESOURCE_HOST_INTERFACES =
            Collections.unmodifiableList( Collections.singletonList( WAN_INTERFACE ) );


    public enum HostType
    {
        RESOURCE_HOST, CONTAINER_HOST, UNKNOWN
    }


    @JsonProperty( "hostId" )
    private String hostId;

    @JsonProperty( "hostName" )
    private String hostName;

    @JsonProperty( "type" )
    private HostType type;

    @JsonProperty( "hostMemory" )
    private MemoryDto memory = new MemoryDto();

    @JsonProperty( "hostCpu" )
    private CpuDto cpu = new CpuDto();

    @JsonProperty( "hostNet" )
    private Map<String, NetDto> net = new HashMap<>();

    @JsonProperty( "hostDisk" )
    private Map<String, DiskDto> disk = new HashMap<>();

    @JsonProperty( "containersCount" )
    private Integer containersCount = 0;

    @JsonProperty( "management" )
    private boolean management;

    @JsonProperty( "createdTime" )
    private Date createdTime = new Date();


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


    public HostType getType()
    {
        return type;
    }


    public void setType( final HostType type )
    {
        this.type = type;
    }


    public MemoryDto getMemory()
    {
        return memory;
    }


    public void setMemory( final MemoryDto memoryDto )
    {
        this.memory = memoryDto;
    }


    public CpuDto getCpu()
    {
        return cpu;
    }


    public void setCpu( final CpuDto cpuDto )
    {
        this.cpu = cpuDto;
    }


    public Map<String, NetDto> getNet()
    {
        return net;
    }


    public void setNet( final Map<String, NetDto> net )
    {
        this.net = net;
    }


    public Map<String, DiskDto> getDisk()
    {
        return disk;
    }


    public void setDisk( final Map<String, DiskDto> disk )
    {
        this.disk = disk;
    }


    public void addDisk( final String partition, final DiskDto diskDto )
    {
        if ( diskDto == null )
        {
            throw new IllegalArgumentException( "Disk DTO could not be null." );
        }

        this.disk.put( partition, diskDto );
    }


    public Integer getContainersCount()
    {
        return containersCount;
    }


    public void setContainersCount( final Integer containersCount )
    {
        this.containersCount = containersCount;
    }


    public boolean isManagement()
    {
        return management;
    }


    public void setManagement( final boolean management )
    {
        this.management = management;
    }


    public Date getCreatedTime()
    {
        return createdTime;
    }


    public void setCreatedTime( final Date createdTime )
    {
        this.createdTime = createdTime;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "HostMetricsDto{" );
        sb.append( "hostId='" ).append( hostId ).append( '\'' );
        sb.append( ", hostName='" ).append( hostName ).append( '\'' );
        sb.append( ", type=" ).append( type );
        sb.append( ", memory=" ).append( memory );
        sb.append( ", cpu=" ).append( cpu );
        sb.append( ", net=" ).append( net );
        sb.append( ", disk=" ).append( disk );
        sb.append( ", containersCount=" ).append( containersCount );
        sb.append( ", management=" ).append( management );
        sb.append( ", createdTime=" ).append( createdTime );
        sb.append( '}' );
        return sb.toString();
    }
}
