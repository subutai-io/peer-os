package io.subutai.bazaar.share.dto;


import java.util.Date;


public class PeerDto
{
    private String id;

    private String name;

    private String ownerId;

    private String fingerprint;

    private Boolean blocked;

    private Date heartbeatDate;


    public PeerDto()
    {
    }


    public PeerDto( final String id, final String name, final String ownerId, final String fingerprint, final Boolean blocked, final Date heartbeatDate )
    {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.fingerprint = fingerprint;
        this.blocked = blocked;
        this.heartbeatDate = heartbeatDate;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getName()
    {
        return name;
    }


    public void setName( final String name )
    {
        this.name = name;
    }


    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    public String getFingerprint()
    {
        return fingerprint;
    }


    public void setFingerprint( final String fingerprint )
    {
        this.fingerprint = fingerprint;
    }


    public Boolean getBlocked()
    {
        return blocked;
    }


    public void setBlocked( final Boolean blocked )
    {
        this.blocked = blocked;
    }


    public Date getHeartbeatDate()
    {
        return heartbeatDate;
    }


    public void setHeartbeatDate( final Date heartbeatDate )
    {
        this.heartbeatDate = heartbeatDate;
    }
}
