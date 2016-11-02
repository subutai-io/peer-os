package io.subutai.hub.share.dto.metrics;


import java.util.Arrays;
import java.util.Collections;
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
        RESOURCE_HOST, CONTAINER_HOST
    }


    @JsonProperty( "hostId" )
    private String hostId;

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


    public String getHostId()
    {
        return hostId;
    }


    public void setHostId( final String hostId )
    {
        this.hostId = hostId;
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
}
