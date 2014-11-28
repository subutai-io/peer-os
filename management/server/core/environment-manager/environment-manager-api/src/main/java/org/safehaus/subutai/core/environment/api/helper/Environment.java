package org.safehaus.subutai.core.environment.api.helper;


import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.environment.api.exception.EnvironmentManagerException;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostKey;
import org.safehaus.subutai.core.peer.api.LocalPeer;

import com.google.common.collect.Sets;


public class Environment
{

    private UUID id;
    private String name;
    transient private Set<ContainerHost> containers;
    private Set<HostKey> hostKeys;
    private EnvironmentStatusEnum status;
    private long creationTimestamp;
    transient private LocalPeer localPeer;


    public Environment( String name, LocalPeer localPeer )
    {
        this.name = name;
        this.localPeer = localPeer;
        this.id = UUIDUtil.generateTimeBasedUUID();
        this.containers = new HashSet<>();
        this.hostKeys = new HashSet<>();
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
        container.setEnvironmentId( id.toString() );
        this.containers.add( container );
        this.hostKeys.add( new HostKey( container.getHostId(), container.getPeerId(), container.getCreatorPeerId(),
                container.getHostname(), container.getEnvironmentId(), container.getNodeGroupName() ) );
    }


    public Set<ContainerHost> getContainers()
    {
        containers = new HashSet<>();
        for ( HostKey hostKey : hostKeys )
        {
            containers.add( localPeer.getContainerHostImpl( hostKey ) );
        }
        return containers;
    }

    //
    //    public void setContainers( final Set<ContainerHost> containers )
    //    {
    //        this.containers = containers;
    //    }


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


    public ContainerHost getContainerHostByUUID( UUID uuid )
    {
        Iterator<ContainerHost> iterator = containers.iterator();
        while ( iterator.hasNext() )
        {
            ContainerHost containerHost = iterator.next();
            if ( containerHost.getId().equals( uuid ) )
            {
                return containerHost;
            }
        }
        return null;
    }


    public ContainerHost getContainerHostByHostname( String hostname )
    {
        Iterator<ContainerHost> iterator = containers.iterator();
        while ( iterator.hasNext() )
        {
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
