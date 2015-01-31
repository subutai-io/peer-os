package org.safehaus.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.core.peer.api.ContainerGroup;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


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


    @OneToMany( mappedBy = "group", fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST }, orphanRemoval = false,
            targetEntity = ContainerHostEntity.class )
    Set<ContainerHost> containerHosts = Sets.newHashSet();


    public ContainerGroupEntity( final UUID environmentId, final UUID initiatorPeerId, final UUID ownerId,
                                 final Set<ContainerHost> containerHosts )
    {
        Preconditions.checkNotNull( environmentId );
        Preconditions.checkNotNull( initiatorPeerId );
        Preconditions.checkNotNull( ownerId );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );

        this.environmentId = environmentId.toString();
        this.initiatorPeerId = initiatorPeerId.toString();
        this.ownerId = ownerId.toString();
        this.containerHosts = containerHosts;
    }


    protected ContainerGroupEntity() {}


    @Override
    public UUID getEnvironmentId()
    {
        return UUID.fromString( environmentId );
    }


    public void setEnvironmentId( final String environmentId )
    {
        this.environmentId = environmentId;
    }


    @Override
    public UUID getInitiatorPeerId()
    {
        return UUID.fromString( initiatorPeerId );
    }


    public void setInitiatorPeerId( final String initiatorPeerId )
    {
        this.initiatorPeerId = initiatorPeerId;
    }


    @Override
    public UUID getOwnerId()
    {
        return UUID.fromString( ownerId );
    }


    public void setOwnerId( final String ownerId )
    {
        this.ownerId = ownerId;
    }


    @Override
    public Set<ContainerHost> getContainerHosts()
    {
        return containerHosts;
    }


    public void setContainerHosts( final Set<ContainerHost> containerHosts )
    {
        this.containerHosts = containerHosts;
    }
}
