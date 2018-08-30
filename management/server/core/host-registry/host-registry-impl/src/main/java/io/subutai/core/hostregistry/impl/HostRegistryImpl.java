package io.subutai.core.hostregistry.impl;


import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Sets;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.IPUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;


/**
 * Implementation of HostRegistry
 */
public class HostRegistryImpl implements HostRegistry
{
    private static final Logger LOG = LoggerFactory.getLogger( HostRegistryImpl.class.getName() );

    private static final String HOST_NOT_CONNECTED_MSG = "Host %s is not connected";
    //timeout after which host expires in seconds

    private static final long HOST_UPDATER_INTERVAL_SEC = 10;

    Set<HostListener> hostListeners = Collections.newSetFromMap( new ConcurrentHashMap<HostListener, Boolean>() );
    ScheduledExecutorService hostUpdater = Executors.newSingleThreadScheduledExecutor();
    ExecutorService threadPool = Executors.newCachedThreadPool();
    Cache<String, ResourceHostInfo> hosts;

    IPUtil ipUtil = new IPUtil();


    @Override
    public void updateResourceHostEntryTimestamp( final String resourceHostId )
    {
        Preconditions.checkNotNull( resourceHostId, " Resource host id is null" );

        hosts.getIfPresent( resourceHostId );
    }


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
    public ContainerHostInfo getContainerHostInfoByContainerName( final String containerName )
            throws HostDisconnectedException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( containerName ), "Invalid container name" );

        for ( ResourceHostInfo resourceHostInfo : hosts.asMap().values() )
        {
            for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
            {
                if ( containerName.equalsIgnoreCase( containerHostInfo.getContainerName() ) )
                {
                    return containerHostInfo;
                }
            }
        }

        throw new HostDisconnectedException( String.format( HOST_NOT_CONNECTED_MSG, containerName ) );
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
    public void removeResourceHost( final String id )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ) );

        hosts.invalidate( id );
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


    void registerHost( ResourceHostInfo newRhInfo, Set<QuotaAlertValue> alerts )
    {
        Preconditions.checkNotNull( newRhInfo, "Info is null" );

        ResourceHostInfo oldRhInfo = hosts.getIfPresent( newRhInfo.getId() );

        if ( oldRhInfo != null )
        {
            ( ( ResourceHostInfoModel ) newRhInfo )
                    .setDateCreated( ( ( ResourceHostInfoModel ) oldRhInfo ).getDateCreated() );
        }
        else
        {
            ( ( ResourceHostInfoModel ) newRhInfo ).setDateCreated( System.currentTimeMillis() );
        }

        hosts.put( newRhInfo.getId(), newRhInfo );

        //notify listeners
        for ( HostListener listener : hostListeners )
        {
            threadPool.execute( new HostNotifier( listener, oldRhInfo, newRhInfo, alerts ) );
        }
    }


    public void init()
    {
        hosts = CacheBuilder.newBuilder().
                expireAfterAccess( HOST_EXPIRATION_SEC, TimeUnit.SECONDS ).
                                    removalListener( new RemovalListener<String, ResourceHostInfo>()
                                    {
                                        @Override
                                        public void onRemoval(
                                                final RemovalNotification<String, ResourceHostInfo> notification )
                                        {
                                            if ( notification.getCause() == RemovalCause.EXPIRED )
                                            {
                                                for ( HostListener listener : hostListeners )
                                                {
                                                    threadPool.execute(
                                                            new HostNotifier( listener, notification.getValue() ) );
                                                }
                                            }
                                        }
                                    } ).
                                    build();

        hostUpdater.scheduleWithFixedDelay( new Runnable()
        {
            @Override
            public void run()
            {
                updateHosts();
            }
        }, HOST_UPDATER_INTERVAL_SEC, HOST_UPDATER_INTERVAL_SEC, TimeUnit.SECONDS );
    }


    public void dispose()
    {
        hosts.invalidateAll();

        threadPool.shutdown();

        hostUpdater.shutdown();
    }


    void updateHosts()
    {
        try
        {
            Set<ResourceHostInfo> cachedResourceHosts = getResourceHostsInfo();

            Set<ResourceHost> registeredResourceHosts = Sets.newHashSet();

            LocalPeer localPeer = getLocalPeer();

            if ( localPeer != null )
            {
                registeredResourceHosts.addAll( localPeer.getResourceHosts() );
            }

            //if local peer has not received heartbeat because it was initialized after the first heartbeat had arrived
            //we need to re-request heartbeat from agent based on cache entries
            if ( cachedResourceHosts.size() > registeredResourceHosts.size() )
            {
                requestHeartbeats( cachedResourceHosts );

                return;
            }
            else if ( localPeer != null )
            {
                boolean noManagement = false;

                try
                {
                    localPeer.getManagementHost();
                }
                catch ( HostNotFoundException e )
                {
                    noManagement = true;
                }

                if ( noManagement )
                {
                    Set<ResourceHostInfo> allHosts = Sets.newHashSet();

                    allHosts.addAll( registeredResourceHosts );

                    allHosts.addAll( cachedResourceHosts );

                    requestHeartbeats( allHosts );

                    return;
                }
            }

            if ( !CollectionUtil.isCollectionEmpty( cachedResourceHosts ) )
            {
                checkAndUpdateHosts( cachedResourceHosts );
            }

            if ( !CollectionUtil.isCollectionEmpty( registeredResourceHosts ) )
            {
                checkAndUpdateHosts( Sets.<ResourceHostInfo>newHashSet( registeredResourceHosts ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking hosts: {}", e.getMessage() );
        }
    }


    private void requestHeartbeats( Set<ResourceHostInfo> resourceHosts )
    {
        for ( final ResourceHostInfo resourceHostInfo : resourceHosts )
        {
            threadPool.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    requestHeartbeat( resourceHostInfo );
                }
            } );
        }
    }


    void checkAndUpdateHosts( Set<ResourceHostInfo> resourceHosts )
    {
        for ( final ResourceHostInfo resourceHostInfo : resourceHosts )
        {
            threadPool.execute( new Runnable()
            {
                @Override
                public void run()
                {
                    updateHost( resourceHostInfo );
                }
            } );
        }
    }


    void updateHost( ResourceHostInfo resourceHostInfo )
    {
        WebClient webClient = null;
        Response response = null;

        try
        {
            webClient = getWebClient( resourceHostInfo.getAddress(), "ping" );

            response = webClient.get();

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                if ( resourceHostInfo instanceof ResourceHost )
                {
                    try
                    {
                        getResourceHostInfoById( resourceHostInfo.getId() );
                    }
                    catch ( HostDisconnectedException e )
                    {
                        requestHeartbeat( resourceHostInfo );
                    }
                }
                else
                {
                    updateResourceHostEntryTimestamp( resourceHostInfo.getId() );
                }
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error checking host {}: {}", resourceHostInfo, e.getMessage() );
        }
        finally
        {
            RestUtil.close( response, webClient );
        }
    }


    void requestHeartbeat( ResourceHostInfo resourceHostInfo )
    {
        WebClient webClient = null;
        Response response = null;

        try
        {
            webClient = getWebClient( resourceHostInfo.getAddress(), "heartbeat" );
            response = webClient.get();
        }
        catch ( Exception e )
        {
            LOG.warn( "Error requesting heartbeat: {}", e.getMessage() );
        }
        finally
        {
            RestUtil.close( response, webClient );
        }
    }


    WebClient getWebClient( String hostIp, String action )
    {
        return RestUtil
                .createWebClient( String.format( "http://%s:%d/%s", hostIp, Common.DEFAULT_AGENT_PORT, action ), 3000,
                        5000, 1 );
    }


    @Override
    public boolean pingHost( String hostIp )
    {
        WebClient webClient = null;
        Response response = null;

        try
        {
            webClient = getWebClient( hostIp, "ping" );

            response = webClient.get();

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                return true;
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error pinging host at ip {}: {}", hostIp, e.getMessage() );
        }
        finally
        {
            RestUtil.close( response, webClient );
        }

        return false;
    }


    LocalPeer getLocalPeer()
    {
        return ServiceLocator.getServiceOrNull( LocalPeer.class );
    }
}
