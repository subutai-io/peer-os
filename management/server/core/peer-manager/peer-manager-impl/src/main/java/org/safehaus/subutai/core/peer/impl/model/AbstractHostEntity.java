package org.safehaus.subutai.core.peer.impl.model;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.safehaus.subutai.common.exception.SubutaiException;


@MappedSuperclass
@Access( AccessType.FIELD )
public class AbstractHostEntity implements Serializable
{

    @Id
    private String id;
    @Column
    private String peerId;
    @Column
    private String hostname;


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getPeerId()
    {
        return peerId;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    public String getHostname()
    {
        return hostname;
    }


    public void setHostname( final String hostname )
    {
        this.hostname = hostname;
    }

    public void calculate() throws SubutaiException {
        throw new SubutaiException( "Unsupported method." );
    }
}
