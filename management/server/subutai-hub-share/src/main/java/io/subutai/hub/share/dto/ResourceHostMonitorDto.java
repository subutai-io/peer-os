package io.subutai.hub.share.dto;


public class ResourceHostMonitorDto
{
    private String peerId;

    private String hostId;

    private String name;

    private Double totalRam;

    private Double availableRam;

    private Double freeRam;

    private Double totalSpace;

    private Double availableSpace;

    private Double usedCpu;


    public ResourceHostMonitorDto()
    {
    }


    public ResourceHostMonitorDto( final String peerId, final String hostId, final String name, final Double totalRam,
                                   final Double availableRam, final Double freeRam, final Double totalSpace,
                                   final Double availableSpace, final Double usedCpu )
    {
        this.peerId = peerId;
        this.hostId = hostId;
        this.name = name;
        this.totalRam = totalRam;
        this.availableRam = availableRam;
        this.freeRam = freeRam;
        this.totalSpace = totalSpace;
        this.availableSpace = availableSpace;
        this.usedCpu = usedCpu;
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


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public Double getTotalRam()
    {
        return totalRam;
    }


    public void setTotalRam( final Double totalRam )
    {
        this.totalRam = totalRam;
    }


    public Double getAvailableRam()
    {
        return availableRam;
    }


    public void setAvailableRam( final Double availableRam )
    {
        this.availableRam = availableRam;
    }


    public Double getFreeRam()
    {
        return freeRam;
    }


    public void setFreeRam( final Double freeRam )
    {
        this.freeRam = freeRam;
    }


    public Double getTotalSpace()
    {
        return totalSpace;
    }


    public void setTotalSpace( final Double totalSpace )
    {
        this.totalSpace = totalSpace;
    }


    public Double getAvailableSpace()
    {
        return availableSpace;
    }


    public void setAvailableSpace( final Double availableSpace )
    {
        this.availableSpace = availableSpace;
    }


    public Double getUsedCpu()
    {
        return usedCpu;
    }


    public void setUsedCpu( final Double usedCpu )
    {
        this.usedCpu = usedCpu;
    }
}
