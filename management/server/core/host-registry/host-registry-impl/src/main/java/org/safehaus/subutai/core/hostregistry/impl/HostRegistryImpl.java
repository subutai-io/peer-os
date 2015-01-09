package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostDisconnectedException;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.HostRegistryException;
import org.safehaus.subutai.core.hostregistry.api.ResourceHostInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;


/**
 * Implementation of ContainerRegistry
 */
public class HostRegistryImpl implements HostRegistry
{
    private static final Logger LOG = LoggerFactory.getLogger( HostRegistryImpl.class.getName() );
    private static final String HOST_NOT_CONNECTED_MSG = "Host %s is not connected";
    //timeout after which host expires in seconds
    private final int hostExpiration;

    private final Broker broker;

    protected Set<HostListener> hostListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<HostListener, Boolean>() );
    protected ExecutorService notifier = Executors.newCachedThreadPool();
    protected HeartBeatListener heartBeatListener;
    protected Cache<UUID, ResourceHostInfo> hosts;


    public HostRegistryImpl( final Broker broker, final int hostExpiration )
    {
        Preconditions.checkNotNull( broker, "Broker is null" );
        Preconditions.checkArgument( hostExpiration > 0, "Host expiration timeout must be greater than 0" );

        this.broker = broker;
        this.hostExpiration = hostExpiration;
        this.heartBeatListener = new HeartBeatListener( this );
    }


    @Override
    public ContainerHostInfo getContainerHostInfoById( final UUID id ) throws HostDisconnectedException
    {
        Preconditions.checkNotNull( id, "Id is null" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
            {
                if ( id.equals( containerHostInfo.getId() ) )
                {
                    return containerHostInfo;
                }
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, id.toString() ) );
    }


    @Override
    public ContainerHostInfo getContainerHostInfoByHostname( final String hostname ) throws HostDisconnectedException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
            {
                if ( hostname.equalsIgnoreCase( containerHostInfo.getHostname() ) )
                {
                    return containerHostInfo;
                }
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, hostname ) );
    }


    @Override
    public Set<ContainerHostInfo> getContainerHostsInfo()
    {
        Set<ContainerHostInfo> containersInfo = Sets.newHashSet();

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            containersInfo.addAll( resourceHostInfo.getContainers() );
        }

        return containersInfo;
    }


    @Override
    public ResourceHostInfo getResourceHostInfoById( final UUID id ) throws HostDisconnectedException
    {
        Preconditions.checkNotNull( id, "Id is null" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            if ( id.equals( resourceHostInfo.getId() ) )
            {
                return resourceHostInfo;
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, id.toString() ) );
    }


    @Override
    public ResourceHostInfo getResourceHostInfoByHostname( final String hostname ) throws HostDisconnectedException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            if ( hostname.equalsIgnoreCase( resourceHostInfo.getHostname() ) )
            {
                return resourceHostInfo;
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, hostname ) );
    }


    @Override
    public Set<ResourceHostInfo> getResourceHostsInfo()
    {
        return Sets.newHashSet( hosts.asMap().values() );
    }


    @Override
    public ResourceHostInfo getResourceHostByContainerHost( final ContainerHostInfo containerHostInfo )
            throws HostDisconnectedException
    {
        Preconditions.checkNotNull( containerHostInfo, "Container host info is null" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo1 : resourceHostInfo.getContainers() )
            {
                if ( containerHostInfo.getId().equals( containerHostInfo1.getId() ) )
                {
                    return resourceHostInfo;
                }
            }
        }

        throw new HostDisconnectedException( "Host is not connected" );
    }


    @Override
    public HostInfo getHostInfoById( final UUID hostId ) throws HostDisconnectedException
    {
        try
        {
            return getContainerHostInfoById( hostId );
        }
        catch ( HostDisconnectedException e )
        {
            LOG.debug( "Host not found among containers", e );

            return getResourceHostInfoById( hostId );
        }
    }


    @Override
    public void addHostListener( final HostListener listener )
    {
        Preconditions.checkNotNull( listener );

        hostListeners.add( listener );
    }


    @Override
    public void removeHostListener( final HostListener listener )
    {
        Preconditions.checkNotNull( listener );

        hostListeners.remove( listener );
    }


    protected void registerHost( ResourceHostInfo info )
    {
        Preconditions.checkNotNull( info, "Info is null" );

        hosts.put( info.getId(), info );

        //notify listeners
        for ( HostListener listener : hostListeners )
        {
            notifier.execute( new HostNotifier( listener, info ) );
        }
    }


    public void init() throws HostRegistryException
    {
        try
        {
            broker.addByteMessageListener( heartBeatListener );

            hosts = CacheBuilder.newBuilder().
                    expireAfterWrite( hostExpiration, TimeUnit.SECONDS ).
                                        build();
        }
        catch ( BrokerException e )
        {
            LOG.error( "Error in init", e );
            throw new HostRegistryException( e );
        }
    }


    public void dispose()
    {
        broker.removeMessageListener( heartBeatListener );
        hosts.invalidateAll();
        notifier.shutdown();
    }
}
