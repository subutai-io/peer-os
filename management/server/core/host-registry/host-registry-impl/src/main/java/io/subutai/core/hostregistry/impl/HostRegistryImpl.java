package io.subutai.core.hostregistry.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.hostregistry.api.HostRegistryException;


/**
 * Implementation of ContainerRegistry
 */
public class HostRegistryImpl implements HostRegistry
{
    private static final String HOST_NOT_CONNECTED_MSG = "Host %s is not connected";
    //timeout after which host expires in seconds
    private static final int hostExpiration = 40;

    protected Set<HostListener> hostListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<HostListener, Boolean>() );
    protected ExecutorService notifier = Executors.newCachedThreadPool();
    protected Cache<String, ResourceHostInfo> hosts;


    @Override
    public ContainerHostInfo getContainerHostInfoById( final String id ) throws HostDisconnectedException
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

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, id ) );
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
    public ResourceHostInfo getResourceHostInfoById( final String id ) throws HostDisconnectedException
    {
        Preconditions.checkNotNull( id, "Id is null" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            if ( id.equals( resourceHostInfo.getId() ) )
            {
                return resourceHostInfo;
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, id ) );
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
    public HostInfo getHostInfoById( final String hostId ) throws HostDisconnectedException
    {
        try
        {
            return getContainerHostInfoById( hostId );
        }
        catch ( HostDisconnectedException e )
        {
            return getResourceHostInfoById( hostId );
        }
    }


    @Override
    public void addHostListener( final HostListener listener )
    {
        if ( listener != null )
        {
            hostListeners.add( listener );
        }
    }


    @Override
    public void removeHostListener( final HostListener listener )
    {
        if ( listener != null )
        {
            hostListeners.remove( listener );
        }
    }


    protected void registerHost( ResourceHostInfo info, Set<QuotaAlertValue> alerts )
    {
        Preconditions.checkNotNull( info, "Info is null" );

        hosts.put( info.getId(), info );

        //notify listeners
        for ( HostListener listener : hostListeners )
        {
            notifier.execute( new HostNotifier( listener, info, alerts ) );
        }
    }


    public void init() throws HostRegistryException
    {
        hosts = CacheBuilder.newBuilder().
                expireAfterWrite( hostExpiration, TimeUnit.SECONDS ).
                                    build();
    }


    public void dispose()
    {
        hosts.invalidateAll();
        notifier.shutdown();
    }
}
