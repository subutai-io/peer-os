package io.subutai.hub.share.dto;


import java.util.Date;


public class ResourceHostMonitorDto
{
    private String peerId;

    private String hostId;

    private String name;

    private Double totalRam;

    private Double availableRam;

    private Double totalSpace;

    private Double availableSpace;

    private Double usedCpu;

    private Date created = new Date();


    public ResourceHostMonitorDto()
    {
    }


    public ResourceHostMonitorDto( final String peerId, final String hostId, final String name, final Double totalRam,
                                   final Double availableRam, final Double totalSpace, final Double availableSpace,
                                   final Double usedCpu, final Date created )
    {
        this.peerId = peerId;
        this.hostId = hostId;
        this.name = name;
        this.totalRam = totalRam;
        this.availableRam = availableRam;
        this.totalSpace = totalSpace;
        this.availableSpace = availableSpace;
        this.usedCpu = usedCpu;
        this.created = created;
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


    public Date getCreated()
    {
        return created;
    }


    public void setCreated( final Date created )
    {
        this.created = created;
    }


    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer( "ResourceHostMonitorDto{" );
        sb.append( "peerId='" ).append( peerId ).append( '\'' );
        sb.append( ", hostId='" ).append( hostId ).append( '\'' );
        sb.append( ", name='" ).append( name ).append( '\'' );
        sb.append( ", totalRam=" ).append( totalRam );
        sb.append( ", availableRam=" ).append( availableRam );
        sb.append( ", totalSpace=" ).append( totalSpace );
        sb.append( ", availableSpace=" ).append( availableSpace );
        sb.append( ", usedCpu=" ).append( usedCpu );
        sb.append( ", created=" ).append( created );
        sb.append( '}' );
        return sb.toString();
    }
}
