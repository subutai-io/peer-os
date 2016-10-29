package io.subutai.hub.share.dto.metrics;


import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;


public class HostMetricsDto
{
    public enum HostType
    {
        RESOURCE_HOST, CONTAINER_HOST
    }


    @JsonProperty( "host_id" )
    private String hostId;

    @JsonProperty( "type" )
    private HostType type;

    @JsonProperty( "host_memory" )
    private MemoryDto memory = new MemoryDto();

    @JsonProperty( "host_cpu" )
    private CpuDto cpuDto = new CpuDto();

    @JsonProperty( "host_net" )
    private Map<String, BigDecimal> net = new HashMap<>();

    @JsonProperty( "host_disk" )
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


    public CpuDto getCpuDto()
    {
        return cpuDto;
    }


    public void setCpuDto( final CpuDto cpuDto )
    {
        this.cpuDto = cpuDto;
    }


    public Map<String, BigDecimal> getNet()
    {
        return net;
    }


    public void setNet( final Map<String, BigDecimal> net )
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
