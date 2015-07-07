package io.subutai.core.peer.impl.entity;


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
@IdClass(PeerDataPk.class)

public class PeerData
{
    @Id
    @Column( name = "id" )
    private String id;

    @Id
    @Column( name = "source" )
    private String source;

    @Lob
    @Column( name = "info" )
    private String info;


    public String getId()
    {
        return id;
    }


    public void setId( final String id )
    {
        this.id = id;
    }


    public String getSource()
    {
        return source;
    }


    public void setSource( final String source )
    {
        this.source = source;
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
