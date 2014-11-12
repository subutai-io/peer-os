package org.safehaus.subutai.core.hostregistry.impl;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.hostregistry.api.ContainerHostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostInfo;
import org.safehaus.subutai.core.hostregistry.api.HostListener;
import org.safehaus.subutai.core.hostregistry.api.HostRegistry;
import org.safehaus.subutai.core.hostregistry.api.HostRegistryException;
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
    //timeout after which host expires in seconds
    private static final int HOST_EXPIRATION = 60;

    private final Broker broker;

    protected Set<HostListener> hostListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<HostListener, Boolean>() );
    protected Executor notifier = Executors.newCachedThreadPool();
    protected HeartBeatListener heartBeatListener;
    protected Cache<UUID, HostInfo> hosts;


    public HostRegistryImpl( final Broker broker )
    {
        Preconditions.checkNotNull( broker, "Broker is null" );

        this.broker = broker;
        this.heartBeatListener = new HeartBeatListener( this );
    }


    @Override
    public ContainerHostInfo getContainerInfoById( final UUID id )
    {
        Preconditions.checkNotNull( id, "Id is null" );

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo : hostInfo.getContainers() )
            {
                if ( id.equals( containerHostInfo.getId() ) )
                {
                    return containerHostInfo;
                }
            }
        }
        return null;
    }


    @Override
    public ContainerHostInfo getContainerInfoByHostname( final String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo : hostInfo.getContainers() )
            {
                if ( hostname.equalsIgnoreCase( containerHostInfo.getHostname() ) )
                {
                    return containerHostInfo;
                }
            }
        }

        return null;
    }


    @Override
    public Set<ContainerHostInfo> getContainersInfo()
    {
        Set<ContainerHostInfo> containersInfo = Sets.newHashSet();

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            containersInfo.addAll( hostInfo.getContainers() );
        }

        return containersInfo;
    }


    @Override
    public HostInfo getHostInfoById( final UUID id )
    {
        Preconditions.checkNotNull( id, "Id is null" );

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            if ( id.equals( hostInfo.getId() ) )
            {
                return hostInfo;
            }
        }

        return null;
    }


    @Override
    public HostInfo getHostInfoByHostname( final String hostname )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            if ( hostname.equalsIgnoreCase( hostInfo.getHostname() ) )
            {
                return hostInfo;
            }
        }

        return null;
    }


    @Override
    public Set<HostInfo> getHostsInfo()
    {
        return Sets.newHashSet( hosts.asMap().values() );
    }


    @Override
    public HostInfo getParentByChild( final ContainerHostInfo containerHostInfo )
    {
        Preconditions.checkNotNull( containerHostInfo, "Container host info is null" );

        for ( HostInfo hostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo1 : hostInfo.getContainers() )
            {
                if ( containerHostInfo.getId().equals( containerHostInfo1.getId() ) )
                {
                    return hostInfo;
                }
            }
        }

        return null;
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


    protected void registerHost( HostInfo info )
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
                    expireAfterWrite( HOST_EXPIRATION, TimeUnit.SECONDS ).
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
    }
}
