package org.safehaus.subutai.core.environment.api.helper;


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;

import com.google.common.collect.Sets;

public class Environment
{

    private UUID id;
    private String name;
    private Set<ContainerHost> containers;
    private EnvironmentStatusEnum status;
    private long creationTimestamp;


    public Environment( String name )
    {
        this.name = name;
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.containers = new HashSet<>();
        this.status = EnvironmentStatusEnum.EMPTY;
        this.creationTimestamp = System.currentTimeMillis();
    }


    public long getCreationTimestamp()
    {
        return creationTimestamp;
    }


    public EnvironmentStatusEnum getStatus()
    {
        return status;
    }


    public void setStatus( final EnvironmentStatusEnum status )
    {
        this.status = status;
    }


    public void addContainer( ContainerHost container )
    {
        container.setEnvironmentId( id );
        this.containers.add( container );
    }


    public Set<ContainerHost> getContainers()
    {
        return containers;
    }


    public void setContainers( final Set<ContainerHost> containers )
    {
        this.containers = containers;
    }


    public void destroyContainer( UUID containerId ) throws EnvironmentManagerException
    {
        //TODO Baha fill in the logic
    }


    public String getName()
    {
        return name;
    }


    public UUID getId()
    {
        return id;
    }


    public ContainerHost getContainerHostByUUID( UUID uuid ) {
        Iterator<ContainerHost> iterator = containers.iterator();
        while ( iterator.hasNext() ) {
            ContainerHost containerHost = iterator.next();
            if ( containerHost.getId().equals( uuid ) )
                return containerHost;
        }
        return null;
    }


    public ContainerHost getContainerHostByHostname( String hostname )
    {
        Iterator<ContainerHost> iterator = containers.iterator();
        while ( iterator.hasNext() ) {
            ContainerHost containerHost = iterator.next();
            if ( containerHost.getHostname().equalsIgnoreCase( hostname ) )
            {
                return containerHost;
            }
        }
        return null;
    }


    public Set<ContainerHost> getHostsByIds( Set<UUID> ids )
    {
        Set<ContainerHost> hosts = Sets.newHashSet();
        for ( UUID id : ids )
        {
            ContainerHost host = getContainerHostByUUID( id );
            if ( host != null )
            {
                hosts.add( host );
            }
        }
        return hosts;
    }


    public void addContainers( final Set<ContainerHost> containerHosts )
    {
        this.containers.addAll( containerHosts );
    }


    public void removeContainer( final ContainerHost containerHost )
    {
        this.containers.remove( containerHost );
    }
}
