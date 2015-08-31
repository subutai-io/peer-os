package io.subutai.core.registration.impl.entity;


import java.sql.Timestamp;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import io.subutai.core.registration.api.service.ContainerToken;


/**
 * Created by talas on 8/28/15.
 */
@Entity
@Table( name = "containers_token" )
@Access( AccessType.FIELD )
public class ContainerTokenImpl implements ContainerToken
{
    @Id
    @Column( name = "container_token" )
    private String token;

    @Column( name = "container_host_id" )
    private String hostId;

    @Column( name = "date_created" )
    private Timestamp dateCreated;

    @Column( name = "ttl" )
    private Long ttl;


    public ContainerTokenImpl()
    {
    }


    public ContainerTokenImpl( final String token, final Timestamp dateCreated, final Long ttl )
    {
        this.token = token;
        this.hostId = "";
        this.dateCreated = dateCreated;
        this.ttl = ttl;
    }


    @Override
    public String getHostId()
    {
        return hostId;
    }


    @Override
    public String getToken()
    {
        return token;
    }


    @Override
    public Timestamp getDateCreated()
    {
        return dateCreated;
    }


    @Override
    public Long getTtl()
    {
        return ttl;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ContainerTokenImpl ) )
        {
            return false;
        }

        final ContainerTokenImpl that = ( ContainerTokenImpl ) o;

        return token.equals( that.token );
    }


    @Override
    public int hashCode()
    {
        return token.hashCode();
    }


    @Override
    public String toString()
    {
        return "ContainerTokenImpl{" +
                "token='" + token + '\'' +
                ", dateCreated=" + dateCreated +
                ", ttl=" + ttl +
                '}';
    }
}
