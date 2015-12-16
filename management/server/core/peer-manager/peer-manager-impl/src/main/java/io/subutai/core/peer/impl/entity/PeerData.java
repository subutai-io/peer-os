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

    @Column( name = "key_phrase" )
    private String keyPhrase;

    @Lob
    @Column( name = "info" )
    private String info;

    @Lob
    @Column( name = "policy" )
    private String policy;


    public PeerData( final String id, final String info, final String keyPhrase, final String policy )
    {
        this.id = id;
        this.info = info;
        this.keyPhrase = keyPhrase;
        this.policy = policy;
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


    public String getKeyPhrase()
    {
        return keyPhrase;
    }


    public void setKeyPhrase( final String keyPhrase )
    {
        this.keyPhrase = keyPhrase;
    }


    public String getPolicy()
    {
        return policy;
    }


    public void setPolicy( final String policy )
    {
        this.policy = policy;
    }
}
