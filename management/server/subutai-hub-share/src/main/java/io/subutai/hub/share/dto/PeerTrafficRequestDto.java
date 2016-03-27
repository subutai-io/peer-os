package io.subutai.hub.share.dto;


import java.util.Date;


public class PeerTrafficRequestDto
{

    private String remoteIp;

    private String remotePeerId;

    private String requestDirection;

    private Date requestDate;

    private Double dataSize;


    public String getRemoteIp()
    {
        return remoteIp;
    }


    public void setRemoteIp( final String remoteIp )
    {
        this.remoteIp = remoteIp;
    }


    public String getRemotePeerId()
    {
        return remotePeerId;
    }


    public void setRemotePeerId( final String remotePeerId )
    {
        this.remotePeerId = remotePeerId;
    }


    public String getRequestDirection()
    {
        return requestDirection;
    }


    public void setRequestDirection( final String requestDirection )
    {
        this.requestDirection = requestDirection;
    }


    public Date getRequestDate()
    {
        return requestDate;
    }


    public void setRequestDate( final Date requestDate )
    {
        this.requestDate = requestDate;
    }


    public Double getDataSize()
    {
        return dataSize;
    }


    public void setDataSize( final Double dataSize )
    {
        this.dataSize = dataSize;
    }
}
