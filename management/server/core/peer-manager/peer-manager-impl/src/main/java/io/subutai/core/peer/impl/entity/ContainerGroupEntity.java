package io.subutai.core.peer.impl.entity;


import java.util.Set;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.util.CollectionUtil;
import io.subutai.core.peer.api.ContainerGroup;


/**
 * ContainerGroup class.
 */
@Entity
@Table( name = "container_group" )
@Access( AccessType.FIELD )
public class ContainerGroupEntity implements ContainerGroup
{
    @Id
    @Column( name = "env_id", nullable = false )
    private String environmentId;
    @Column( name = "initiator_peer_id", nullable = false )
    private String initiatorPeerId;
    @Column( name = "owner_id", nullable = false )
    private String ownerId;

    @ElementCollection( targetClass = String.class, fetch = FetchType.EAGER )
    private Set<String> containerIds = Sets.newHashSet();


    public ContainerGroupEntity( final String environmentId, final String initiatorPeerId, final String ownerId )
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkNotNull( initiatorPeerId );
        Preconditions.checkNotNull( ownerId );

        this.environmentId = environmentId;
        this.initiatorPeerId = initiatorPeerId;
        this.ownerId = ownerId;
    }


    protected ContainerGroupEntity() {}


    @Override
    public String getEnvironmentId()
    {
        return environmentId;
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public String getInitiatorPeerId()
    {
        return initiatorPeerId;
    }


    public void setInitiatorPeerId( final String initiatorPeerId )
    {
        this.initiatorPeerId = initiatorPeerId;
    }


    @Override
    public String getOwnerId()
    {
        return ownerId;
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    @Override
    public Set<String> getContainerIds()
    {
        Set<String> ids = Sets.newHashSet();

        for ( String id : containerIds )
        {
            ids.add( id );
        }

        return ids;
    }


    public void setContainerIds( final Set<String> containerIds )

    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerIds ), "Invalid container ids set" );

        Set<String> ids = Sets.newHashSet();

        for ( String id : containerIds )
        {
            ids.add( id );
        }

        this.containerIds = ids;
    }
}
