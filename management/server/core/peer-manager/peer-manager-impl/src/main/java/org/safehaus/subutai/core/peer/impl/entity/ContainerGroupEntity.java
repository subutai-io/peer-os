package org.safehaus.subutai.core.peer.impl.entity;


import java.util.Set;
import java.util.UUID;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.core.peer.api.ContainerGroup;

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


    @OneToMany( mappedBy = "group", fetch = FetchType.EAGER,
            targetEntity = ContainerHostEntity.class )
    Set<ContainerHost> containerHosts = Sets.newHashSet();


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
