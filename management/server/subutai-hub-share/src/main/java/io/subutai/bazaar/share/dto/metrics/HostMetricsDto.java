package io.subutai.bazaar.share.dto.metrics;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


public class HostMetricsDto
{
    public static final String CURRENT = "current";


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

    @JsonProperty( "startTime" )
    private Date startTime;

    @JsonProperty( "endTime" )
    private Date endTime;

    @JsonProperty( "osName" )
    private String osName;

    @JsonProperty( "ipAddress" )
    private String ipAddress;

    @JsonIgnore
    private transient Long dbId = null;


    public Long getDbId()
    {
        return dbId;
    }


    public void setDbId( final long dbId )
    {
        this.dbId = dbId;
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


    public Date getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Date startTime )
    {
        this.startTime = startTime;
    }


    public Date getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Date endTime )
    {
        this.endTime = endTime;
    }


    public String getOsName()
    {
        return osName;
    }


    public void setOsName( final String osName )
    {
        this.osName = osName;
    }


    public String getIpAddress()
    {
        return ipAddress;
    }


    public void setIpAddress( final String ipAddress )
    {
        this.ipAddress = ipAddress;
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
        sb.append( ", osName=" ).append( osName );
        sb.append( ", management=" ).append( management );
        sb.append( ", createdTime=" ).append( createdTime );
        sb.append( ", startTime=" ).append( startTime );
        sb.append( ", endTime=" ).append( endTime );
        sb.append( ", ipAddress=" ).append( ipAddress );
        sb.append( '}' );
        return sb.toString();
    }
}
