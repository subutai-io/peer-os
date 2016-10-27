package io.subutai.hub.share.dto.host;


import java.util.Set;

import io.subutai.hub.share.dto.HostInterfaceDto;


public class ResourceHostMetricDto
{
    private String peerId;

    private String hostId;

    private Boolean state; //State of Resource Host -> 0 (Offline), 1 (Online)

    private String name;

    private Double memory; //RAM in Megabyte

    private Double disk; //Disk memory in Gigabytes

    private String cpuModel;

    private Double cpuFrequency; //frequency in GHz

    private int cpuCore; //Number of cores in CPU

    private Boolean hasAccessFromInternet; // Has access to machine from internet

    private Set<HostInterfaceDto> interfaces;


    public ResourceHostMetricDto()
    {
    }


    public ResourceHostMetricDto( final String peerId, final String hostId, final Boolean state, final String name,
                                  final Double memory, final Double disk, final String cpuModel,
                                  final Double cpuFrequency, final int cpuCore )
    {
        this.peerId = peerId;
        this.hostId = hostId;
        this.state = state;
        this.name = name;
        this.memory = memory;
        this.disk = disk;
        this.cpuModel = cpuModel;
        this.cpuFrequency = cpuFrequency;
        this.cpuCore = cpuCore;
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


    public Boolean getState()
    {
        return state;
    }


    public void setState( final Boolean state )
    {
        this.state = state;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Double getMemory()
    {
        return memory;
    }


    public void setMemory( final Double memory )
    {
        this.memory = memory;
    }


    public Double getDisk()
    {
        return disk;
    }


    public void setDisk( final Double disk )
    {
        this.disk = disk;
    }


    public String getCpuModel()
    {
        return cpuModel;
    }


    public void setCpuModel( final String cpuModel )
    {
        this.cpuModel = cpuModel;
    }


    public Double getCpuFrequency()
    {
        return cpuFrequency;
    }


    public void setCpuFrequency( final Double cpuFrequency )
    {
        this.cpuFrequency = cpuFrequency;
    }


    public int getCpuCore()
    {
        return cpuCore;
    }


    public void setCpuCore( final int cpuCore )
    {
        this.cpuCore = cpuCore;
    }


    public Boolean getHasAccessFromInternet()
    {
        return hasAccessFromInternet;
    }


    public void setHasAccessFromInternet( final Boolean hasAccessFromInternet )
    {
        this.hasAccessFromInternet = hasAccessFromInternet;
    }


    public Set<HostInterfaceDto> getInterfaces()
    {
        return interfaces;
    }


    public void setInterfaces( final Set<HostInterfaceDto> interfaces )
    {
        this.interfaces = interfaces;
    }
}
