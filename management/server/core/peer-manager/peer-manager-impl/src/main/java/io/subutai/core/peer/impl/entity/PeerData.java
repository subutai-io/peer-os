package io.subutai.core.peer.impl.entity;


import java.io.Serializable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Lob;
import javax.persistence.Table;


@Entity
@Table( name = "peer_data" )
@Access( AccessType.FIELD )
public class PeerData implements Serializable
{
    @Id
    @Column( name = "id" )
    private String id;

    @Lob
    @Column( name = "info" )
    private String info;


    public PeerData( final String id, final String info )
    {
        this.id = id;
        this.info = info;
    }


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getInfo()
    {
        return info;
    }


    public void setInfo( final String info )
    {
        this.info = info;
    }
}
