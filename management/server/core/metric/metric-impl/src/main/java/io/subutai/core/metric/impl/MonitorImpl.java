package io.subutai.core.metric.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.exception.DaoException;
import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.common.metric.ContainerHostQuotaState;
import io.subutai.common.metric.HistoricalMetric;
import io.subutai.common.metric.HostMetric;
import io.subutai.common.metric.MetricType;
import io.subutai.common.metric.OwnerResourceUsage;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ManagementHost;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.metric.api.AlertListener;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    private static final String ENVIRONMENT_IS_NULL_MSG = "Environment is null";
    private static final String CONTAINER_IS_NULL_MSG = "Container is null";
    private static final String INVALID_SUBSCRIBER_ID_MSG = "Invalid subscriber id";
    private static final String SETTINGS_IS_NULL_MSG = "Settings is null";

    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class );

    //set of metric subscribers
    protected Set<AlertListener> alertListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<AlertListener, Boolean>() );
    private final Commands commands = new Commands();
    private final PeerManager peerManager;
    private final EnvironmentManager environmentManager;
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MonitorDao monitorDao;
    protected DaoManager daoManager;
    private Map<String, BaseMetric> metrics = new ConcurrentHashMap<>();
//    protected ScheduledExecutorService stateUpdateExecutorService;


    public MonitorImpl( PeerManager peerManager, DaoManager daoManager, EnvironmentManager environmentManager )
            throws MonitorException
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkNotNull( environmentManager );

        try
        {
            this.daoManager = daoManager;
            this.monitorDao = new MonitorDao( daoManager.getEntityManagerFactory() );
            this.peerManager = peerManager;
            this.environmentManager = environmentManager;
            //            peerManager.addRequestListener( new RemoteAlertListener( this ) );
            //            peerManager.addRequestListener( new RemoteMetricRequestListener( this ) );
            //            peerManager.addRequestListener( new MonitoringActivationListener( this, peerManager ) );
        }
        catch ( DaoException e )
        {
            throw new MonitorException( e );
        }

//        stateUpdateExecutorService = Executors.newScheduledThreadPool( 1 );
//        stateUpdateExecutorService.scheduleWithFixedDelay( new MetricsUpdater( this ), 10, 90, TimeUnit.SECONDS );
    }


    @Override
    public Set<ContainerHostMetric> getContainerHostsMetrics( final Environment environment ) throws MonitorException
    {
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );

        Set<ContainerHostMetric> metrics = new HashSet<>();

        //obtain environment containers
        Set<EnvironmentContainerHost> containerHosts = environment.getContainerHosts();

        Set<Peer> peers = Sets.newHashSet();

        //determine containers' host peers
        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                peers.add( containerHost.getPeer() );
            }
            catch ( Exception e )
            {
                LOG.error( String.format( "Could not obtain peer for container %s", containerHost.getHostname() ), e );
                throw new MonitorException( e );
            }
        }

        //send metric requests to target getPeerInfos
        for ( Peer peer : peers )
        {
            if ( peer.isLocal() )
            {
                //dispatch locally
                metrics.addAll( getLocalContainerHostsMetrics( environment.getId() ) );
            }
            else
            {
                //send remote request
                metrics.addAll( getRemoteContainerHostsMetrics( environment.getId(), peer ) );
            }
        }

        return metrics;
    }


    @Override
    public Set<ContainerHostMetric> getLocalContainerHostsMetrics( final Set<ContainerHost> containerHosts )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );

        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();

        LocalPeer localPeer = peerManager.getLocalPeer();

        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                Preconditions.checkArgument( containerHost.isLocal(),
                        String.format( "Container %s is not local", containerHost.getHostname() ) );

                //                ContainerGroup containerGroup = localPeer.findContainerGroupByContainerId(
                // containerHost.getId() );

                //get container's resource host
                ResourceHost resourceHost = localPeer.getResourceHostByContainerId( containerHost.getId() );

                //get metric
                //TODO: remove resource host argument
                addLocalContainerHostMetric( containerHost.getEnvironmentId(), resourceHost,
                        resourceHost.getContainerHostById( containerHost.getId() ), metrics );
            }
            catch ( Exception e )
            {
                LOG.error( String.format( "Error obtaining metric for container %s", containerHost.getHostname() ), e );
            }
        }

        Set<ContainerHostMetric> result = Sets.newHashSet();
        result.addAll( metrics );

        return result;
    }


    @Override
    public ContainerHostMetric getLocalContainerHostMetric( final ContainerHost containerHost ) throws MonitorException
    {
        Preconditions.checkNotNull( containerHost );

        Set<ContainerHostMetric> metrics = getLocalContainerHostsMetrics( Sets.newHashSet( containerHost ) );

        if ( metrics.isEmpty() )
        {
            throw new MonitorException(
                    String.format( "Failed to obtain metric for container %s", containerHost.getHostname() ) );
        }

        return metrics.iterator().next();
    }


    protected Set<ContainerHostMetricImpl> getRemoteContainerHostsMetrics( String environmentId, Peer peer )
    {
        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();
        try
        {
            //create request for metrics
            ContainerHostMetricRequest request = new ContainerHostMetricRequest( environmentId );

            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            //*************************************************************

            //send request and obtain metrics
            ContainerHostMetricResponse response =
                    peer.sendRequest( request, RecipientType.METRIC_REQUEST_RECIPIENT.name(),
                            Constants.METRIC_REQUEST_TIMEOUT, ContainerHostMetricResponse.class,
                            Constants.METRIC_REQUEST_TIMEOUT, headers );

            //if response contains metrics, add them to result
            if ( response != null && !CollectionUtil.isCollectionEmpty( response.getMetrics() ) )
            {
                metrics.addAll( response.getMetrics() );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( String.format( "Error obtaining metrics from peer %s", peer.getName() ), e );
        }
        return metrics;
    }


    protected Set<ContainerHostMetricImpl> getLocalContainerHostsMetrics( String environmentId )
    {

        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();
        //        try
        //        {
        LocalPeer localPeer = peerManager.getLocalPeer();

        //            ContainerGroup containerGroup = localPeer.findContainerGroupByEnvironmentId( environmentId );

        //obtain environment containers

        Set<ContainerHost> containerHosts = localPeer.findContainersByEnvironmentId( environmentId );
        //            Set<String> containerIds = containerGroup.getContainerIds();
        retrieveContainerHostMetrics( containerHosts, localPeer, environmentId, metrics );
        //        }
        //        catch ( ContainerGroupNotFoundException e )
        //        {
        //            LOG.error( "Error obtaining local container metrics", e );
        //        }
        return metrics;
    }


    private void retrieveContainerHostMetrics( final Set<ContainerHost> containerIds, final LocalPeer localPeer,
                                               final String environmentId, Set<ContainerHostMetricImpl> metrics )
    {
        for ( ContainerHost containerHost : containerIds )
        {
            try
            {
                //get container's resource host
                ResourceHost resourceHost = localPeer.getResourceHostByContainerId( containerHost.getId() );

                //get metric
                //TODO: remove passing resource host
                addLocalContainerHostMetric( environmentId, resourceHost,
                        resourceHost.getContainerHostById( containerHost.getId() ), metrics );
            }
            catch ( HostNotFoundException e )
            {
                LOG.error( String.format( "Host not found by id %s", containerHost.getId() ), e );
            }
        }
    }


    protected void addLocalContainerHostMetric( final String environmentId, final ResourceHost resourceHost,
                                                final ContainerHost localContainer,
                                                Set<ContainerHostMetricImpl> metrics )
    {
        if ( resourceHost != null )
        {
            try
            {
                //execute metrics command
                CommandResult result =
                        resourceHost.execute( commands.getCurrentMetricCommand( localContainer.getHostname() ) );
                if ( result.hasSucceeded() )
                {
                    ContainerHostMetricImpl metric =
                            JsonUtil.fromJson( result.getStdOut(), ContainerHostMetricImpl.class );
                    metric.setEnvironmentId( environmentId );
                    metric.setHostId( localContainer.getId() );
                    metrics.add( metric );
                }
                else
                {
                    LOG.error( String.format( "Error getting metrics from %s: %s", localContainer.getHostname(),
                            result.getStdErr() ) );
                }
            }
            catch ( CommandException | JsonSyntaxException e )
            {
                LOG.error( "Error in addLocalContainerHostMetric", e );
            }
        }
        else
        {
            LOG.error( String.format( "Could not find resource host if %s", localContainer.getHostname() ) );
        }
    }


    @Override
    public Set<ResourceHostMetric> getResourceHostsMetrics()
    {
        Set<ResourceHostMetric> metrics = new HashSet<>();
        //obtain resource hosts
        Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();
        //iterate resource hosts and get their metrics
        for ( ResourceHost resourceHost : resourceHosts )
        {
            addResourceHostMetric( resourceHost, metrics );
        }


        return metrics;
    }


    @Override
    public ResourceHostMetric getResourceHostMetric( ResourceHost resourceHost ) throws MonitorException
    {
        Preconditions.checkNotNull( resourceHost, "Invalid resource host" );

        Set<ResourceHostMetric> metrics = Sets.newHashSet();
        addResourceHostMetric( resourceHost, metrics );

        if ( metrics.isEmpty() )
        {
            throw new MonitorException( "Failed to obtain resource host metric" );
        }

        return metrics.iterator().next();
    }


    protected void addResourceHostMetric( ResourceHost resourceHost, Set<ResourceHostMetric> metrics )
    {
        try
        {
            CommandResult result =
                    resourceHost.execute( commands.getCurrentMetricCommand( resourceHost.getHostname() ) );
            if ( result.hasSucceeded() )
            {
                ResourceHostMetric metric = JsonUtil.fromJson( result.getStdOut(), ResourceHostMetric.class );
                //set peer id for future reference
                metric.setPeerId( peerManager.getLocalPeer().getId() );
                metric.setHostId( resourceHost.getId() );
                metrics.add( metric );
            }
            else
            {
                LOG.error( String.format( "Error getting metrics from resource host %s: %s", resourceHost.getHostname(),
                        result.getStdErr() ) );
            }
        }
        catch ( CommandException | JsonSyntaxException e )
        {
            LOG.error( "Error in addResourceHostMetric", e );
        }
    }

    private HostMetric fetchManagementHostMetric( ManagementHost managementHost )
    {
        HostMetric result = null;
        try
        {

            CommandResult commandResult =
                    managementHost.execute( commands.getCurrentMetricCommand( managementHost.getHostname() ) );
            if ( commandResult.hasSucceeded() )
            {
                result = JsonUtil.fromJson( commandResult.getStdOut(), HostMetric.class );
                result.setPeerId( peerManager.getLocalPeer().getId() );
                result.setHostId( managementHost.getId() );
                result.setHostName( managementHost.getHostname() );
                LOG.debug( String.format( "Host %s metrics fetched successfully.", managementHost.getHostname() ) );
            }
            else
            {
                LOG.warn( String.format( "Error getting %s metrics", managementHost.getHostname() ) );
            }
        }
        catch ( CommandException | JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
        }

        return result;
    }


    private ResourceHostMetric fetchResourceHostMetric( ResourceHost resourceHost )
    {
        ResourceHostMetric result = null;
        try
        {

            CommandResult commandResult =
                    resourceHost.execute( commands.getCurrentMetricCommand( resourceHost.getHostname() ) );
            if ( commandResult.hasSucceeded() )
            {
                result = JsonUtil.fromJson( commandResult.getStdOut(), ResourceHostMetric.class );
                result.setPeerId( peerManager.getLocalPeer().getId() );
                result.setHostId( resourceHost.getId() );
                result.setHostName( resourceHost.getHostname() );
                result.setContainersCount( resourceHost.getContainers().size() );
                LOG.debug( String.format( "Host %s metrics fetched successfully.", resourceHost.getHostname() ) );
            }
            else
            {
                LOG.warn( String.format( "Error getting %s metrics", resourceHost.getHostname() ) );
            }
        }
        catch ( CommandException | JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
        }

        return result;
    }


    private ContainerHostQuotaState fetchContainerHostQuotaState( ResourceHost resourceHost,
                                                                  ContainerHost containerHost )
    {
        ContainerHostQuotaState result = null;
        try
        {

            CommandResult commandResult =
                    resourceHost.execute( commands.getContainerHostQuotaCommand( containerHost.getHostname() ) );
            if ( commandResult.hasSucceeded() )
            {
                result = JsonUtil.fromJson( commandResult.getStdOut(), ContainerHostQuotaState.class );
                result.setPeerId( peerManager.getLocalPeer().getId() );
                result.setHostId( containerHost.getId() );
                result.setHostName( containerHost.getHostname() );

                LOG.debug( String.format( "Container host %s metrics fetched successfully.",
                        containerHost.getHostname() ) );
            }
            else
            {
                LOG.warn(
                        String.format( "Error getting metrics from resource host %s for %s", resourceHost.getHostname(),
                                containerHost.getHostname() ) );
            }
        }
        catch ( CommandException | JsonSyntaxException e )
        {
            LOG.error( e.getMessage(), e );
        }

        return result;
    }


    @Override
    public void startMonitoring( final String subscriberId, final Environment environment,
                                 final MonitoringSettings monitoringSettings ) throws MonitorException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), INVALID_SUBSCRIBER_ID_MSG );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );
        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );


        //make sure subscriber id is truncated to 100 characters
        String trimmedSubscriberId = StringUtil.trimToSize( subscriberId, Constants.MAX_SUBSCRIBER_ID_LEN );

        //save subscription to database
        try
        {
            monitorDao.addSubscription( environment.getId(), trimmedSubscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in startMonitoring", e );
            throw new MonitorException( e );
        }

        //activate monitoring
        Set<ContainerHost> a = new HashSet<>();
        a.addAll( environment.getContainerHosts() );
        activateMonitoring( a, monitoringSettings, environment.getId() );
    }


    @Override
    public void startMonitoring( final String subscriberId, final ContainerHost containerHost,
                                 final MonitoringSettings monitoringSettings ) throws MonitorException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), INVALID_SUBSCRIBER_ID_MSG );
        Preconditions.checkNotNull( containerHost, CONTAINER_IS_NULL_MSG );
        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );

        //make sure subscriber id is truncated to 100 characters
        String trimmedSubscriberId = StringUtil.trimToSize( subscriberId, Constants.MAX_SUBSCRIBER_ID_LEN );


        //save subscription to database
        try
        {
            String environmentId =
                    containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId() : null;
            monitorDao.addSubscription( environmentId, trimmedSubscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in startMonitoring", e );
            throw new MonitorException( e );
        }

        //activate monitoring
        String environmentId =
                containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId() : null;
        activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, environmentId );
    }


    @Override
    public void stopMonitoring( final String subscriberId, final Environment environment ) throws MonitorException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), INVALID_SUBSCRIBER_ID_MSG );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );

        //make sure subscriber id is truncated to 100 characters
        String trimmedSubscriberId = StringUtil.trimToSize( subscriberId, Constants.MAX_SUBSCRIBER_ID_LEN );

        //remove subscription from database
        try
        {
            monitorDao.removeSubscription( environment.getId(), trimmedSubscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in stopMonitoring", e );
            throw new MonitorException( e );
        }
    }


    @Override
    public void activateMonitoring( final ContainerHost containerHost, final MonitoringSettings monitoringSettings/*,
     final String environmentId*/ ) throws MonitorException

    {
        Preconditions.checkNotNull( containerHost, CONTAINER_IS_NULL_MSG );
        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );

        String environmentId =
                containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId() : null;
        activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, environmentId );
    }


    protected void activateMonitoring( Set<ContainerHost> containerHosts, MonitoringSettings monitoringSettings,
                                       String environmentId ) throws MonitorException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
        Map<Peer, Set<ContainerHost>> peersContainers = Maps.newHashMap();

        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                Peer peer = containerHost.getPeer();

                Set<ContainerHost> containers = peersContainers.get( peer );

                if ( containers == null )
                {
                    containers = Sets.newHashSet();
                    peersContainers.put( peer, containers );
                }

                containers.add( containerHost );
            }
            catch ( Exception e )
            {
                LOG.error( String.format( "Could not obtain peer for container %s", containerHost.getHostname() ), e );
                throw new MonitorException( e );
            }
        }


        for ( Map.Entry<Peer, Set<ContainerHost>> peerContainers : peersContainers.entrySet() )
        {
            Peer peer = peerContainers.getKey();
            Set<ContainerHost> containers = peerContainers.getValue();

            if ( peer.isLocal() )
            {
                activateMonitoringAtLocalContainers( containers, monitoringSettings );
            }
            else
            {
                activateMonitoringAtRemoteContainers( peer, containers, monitoringSettings, environmentId );
            }
        }
    }


    protected void activateMonitoringAtRemoteContainers( Peer peer, Set<ContainerHost> containerHosts,
                                                         MonitoringSettings monitoringSettings, String environmentId )
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );

        try
        {
            //*********construct Secure Header ****************************
            Map<String, String> headers = Maps.newHashMap();
            //*************************************************************

            peer.sendRequest( new MonitoringActivationRequest( containerHosts, monitoringSettings ),
                    RecipientType.MONITORING_ACTIVATION_RECIPIENT.name(), Constants.MONITORING_ACTIVATION_TIMEOUT,
                    headers );
        }
        catch ( PeerException e )
        {
            LOG.error( "Error in activateMonitoringAtRemoteContainers", e );
        }
    }


    protected void activateMonitoringAtLocalContainers( Set<ContainerHost> containerHosts,
                                                        MonitoringSettings monitoringSettings )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
        Preconditions.checkNotNull( monitoringSettings );

        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                ResourceHost resourceHost =
                        peerManager.getLocalPeer().getResourceHostByContainerId( containerHost.getId() );
                CommandResult commandResult = resourceHost.execute(
                        commands.getActivateMonitoringCommand( containerHost.getHostname(), monitoringSettings ) );
                if ( !commandResult.hasSucceeded() )
                {
                    LOG.error( String.format( "Error activating metrics on %s: %s %s", containerHost.getHostname(),
                            commandResult.getStatus(), commandResult.getStdErr() ) );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error in activateMonitoringAtLocalContainers", e );
            }
        }
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerId containerId, int pid )
            throws MonitorException
    {
        Preconditions.checkNotNull( containerId );
        Preconditions.checkArgument( pid > 0 );
        try
        {

            Host c = peerManager.getLocalPeer().bindHost( containerId );
            ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostByContainerName( c.getHostname() );

            CommandResult commandResult =
                    resourceHost.execute( commands.getProcessResourceUsageCommand( c.getHostname(), pid ) );
            if ( !commandResult.hasSucceeded() )
            {
                throw new MonitorException(
                        String.format( "Error getting process resource usage of pid=%d on %s: %s %s", pid,
                                c.getHostname(), commandResult.getStatus(), commandResult.getStdErr() ) );
            }

            ProcessResourceUsage result = JsonUtil.fromJson( commandResult.getStdOut(), ProcessResourceUsage.class );
            result.setContainerId( containerId );

            return result;
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Could not obtain process resource usage for container %s, pid %d",
                    containerId.getId(), pid ), e );
            throw new MonitorException( e );
        }
    }


    @Override
    public OwnerResourceUsage getOwnerResourceUsage( final String ownerId ) throws MonitorException
    {
        Preconditions.checkNotNull( ownerId, "'Invalid owner id" );

        LocalPeer localPeer = peerManager.getLocalPeer();
        //        Set<ContainerGroup> containerGroups = localPeer.findContainerGroupsByOwnerId( ownerId );

        Set<ContainerHost> ownerContainers = Sets.newHashSet();

        for ( ResourceHost resourceHost : localPeer.getResourceHosts() )
        {
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                ownerContainers.add( containerHost );
            }
        }

        //        for ( ContainerGroup containerGroup : containerGroups )
        //        {
        //            for ( String containerId : containerGroup.getContainerIds() )
        //            {
        //                try
        //                {
        //                    ownerContainers.add( localPeer.getContainerHostById( containerId ) );
        //                }
        //                catch ( HostNotFoundException e )
        //                {
        //                    LOG.error( String.format( "Host not found by id %s", containerId ), e );
        //                }
        //            }
        //        }

        if ( ownerContainers.isEmpty() )
        {
            throw new MonitorException( String.format( "Could not obtain owner container hosts" ) );
        }

        Set<ContainerHostMetric> ownerContainerMetrics = getLocalContainerHostsMetrics( ownerContainers );

        double usedRam = 0;
        double usedCpu = 0;
        double usedDiskVar = 0;
        double usedDiskHome = 0;
        double usedDiskOpt = 0;
        double usedDiskRootFs = 0;

        for ( ContainerHostMetric ownerContainerMetric : ownerContainerMetrics )
        {
            usedRam += ownerContainerMetric.getUsedRam();
            usedCpu += ownerContainerMetric.getUsedCpu();
            usedDiskHome += ownerContainerMetric.getUsedDiskHome();
            usedDiskOpt += ownerContainerMetric.getUsedDiskOpt();
            usedDiskRootFs += ownerContainerMetric.getUsedDiskRootfs();
            usedDiskVar += ownerContainerMetric.getUsedDiskVar();
        }


        return new OwnerResourceUsage( usedRam, usedCpu, usedDiskRootFs, usedDiskVar, usedDiskHome, usedDiskOpt );
    }


    /**
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertMetric - body of notifyOnAlert in JSON
     */
    @Override
    public void alert( final String alertMetric ) throws MonitorException
    {
        try
        {
            //deserialize container metric
            ContainerHostMetricImpl containerHostMetric =
                    JsonUtil.fromJson( alertMetric, ContainerHostMetricImpl.class );

            LocalPeer localPeer = peerManager.getLocalPeer();

            //find source container host
            ContainerHost containerHost = localPeer.getContainerHostByName( containerHostMetric.getHost() );

            //set host id for future reference
            containerHostMetric.setHostId( containerHost.getId() );

            //find container's initiator peer
            //            ContainerGroup containerGroup = localPeer.findContainerGroupByContainerId( containerHost
            // .getId() );


            //set environment id
            containerHostMetric.setEnvironmentId( containerHost.getEnvironmentId() );


            Peer creatorPeer = peerManager.getPeer( containerHost.getInitiatorPeerId() );

            //if container is "created" by local peer, notifyOnAlert local peer
            if ( creatorPeer.isLocal() )
            {
                notifyOnAlert( containerHostMetric );
            }
            //send metric to remote creator peer
            else
            {

                //*********construct Secure Header ****************************
                Map<String, String> headers = Maps.newHashMap();
                //*************************************************************


                creatorPeer.sendRequest( containerHostMetric, RecipientType.ALERT_RECIPIENT.name(),
                        Constants.ALERT_TIMEOUT, headers );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error in onAlert", e );
            throw new MonitorException( e );
        }
    }


    /**
     * This methods is called by REST endpoint when a remote peer sends a notifyOnAlert from one of its hosted
     * containers belonging to this peer or when local "own" container is under stress
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void notifyOnAlert( final ContainerHostMetricImpl metric ) throws MonitorException
    {
        try
        {
            //obtain user from environment
            Environment environment = environmentManager.loadEnvironment( metric.getEnvironmentId() );
            //User user = identityManager.getUser( environment.getUserId() );

            //if ( user == null )
            {
                throw new MonitorException(
                        String.format( "Failed to retrieve environment's '%s' user", environment.getName() ) );
            }
            //login under him
            //identityManager.loginWithToken( user.getUsername() );

            //search for subscriber if not found then no-op
            //Set<String> subscribersIds = monitorDao.getEnvironmentSubscribersIds( metric.getEnvironmentId() );
            //for ( String subscriberId : subscribersIds )
            //{
            //notify subscriber on alert
            //notifyListener( metric, subscriberId );
            //}
        }
        catch ( MonitorException e )
        {
            throw e;
        }
        catch ( Exception e )
        {
            LOG.error( "Error in notifyOnAlert", e );
            throw new MonitorException( e );
        }
    }


    protected void notifyListener( final ContainerHostMetric metric, String subscriberId )
    {
        for ( final AlertListener listener : alertListeners )
        {
            if ( listener.getSubscriberId().startsWith( subscriberId ) )
            {
                notificationExecutor.execute( new AlertNotifier( metric, listener ) );
            }
        }
    }


    public void destroy()
    {
        notificationExecutor.shutdown();
    }


    /**
     * Adds listener to be notified if threshold within environment is exceeded (after this call, interested parties
     * need to execute startMonitoring call passing some environment under interest).
     */
    public void addAlertListener( AlertListener alertListener )
    {
        if ( alertListener != null )
        {
            alertListeners.add( alertListener );
        }
    }


    public void removeAlertListener( AlertListener alertListener )
    {
        if ( alertListener != null )
        {
            alertListeners.remove( alertListener );
        }
    }


    @Override
    public List<HistoricalMetric> getHistoricalMetric( final Host host, final MetricType metricType )
    {
        Preconditions.checkNotNull( host );
        Preconditions.checkNotNull( metricType );

        List<HistoricalMetric> metrics = new ArrayList<>();

        //execute metrics command
        CommandResult result;
        ResourceHost resourceHost;
        try
        {
            RequestBuilder historicalMetricCommand = commands.getHistoricalMetricCommand( host, metricType );
            if ( !( host instanceof ResourceHost ) )
            {
                resourceHost = peerManager.getLocalPeer().getResourceHostByContainerId( host.getId() );
            }
            else
            {
                resourceHost = ( ResourceHost ) host;
            }
            result = resourceHost.execute( historicalMetricCommand );
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not run command successfully! Error: {}", e );
            return Lists.newArrayList();
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( "Could not find resource host of host {}!", host.getHostname() );
            return Lists.newArrayList();
        }
        if ( result.hasSucceeded() )
        {
            processHistoricalMetricOutput( result.getStdOut(), metricType, metrics, host );
        }
        else
        {
            LOG.error( String.format( "Error getting historical metrics from %s: %s", host.getHostname(),
                    result.getStdErr() ) );
        }

        return metrics;
    }


    private void processHistoricalMetricOutput( final String stdOut, final MetricType metricType,
                                                final List<HistoricalMetric> metrics, final Host host )
    {
        String[] lines = stdOut.split( "\\r?\\n" );
        int timestamp;
        double value;
        for ( String line : lines )
        {
            int seperatorIndex = line.indexOf( ":" );
            timestamp = Integer.parseInt( line.substring( 0, seperatorIndex ) );
            value = Double.parseDouble( line.substring( seperatorIndex + 1 ).trim() );
            switch ( metricType )
            {
                case RAM:
                case DISK_HOME:
                case DISK_OPT:
                case DISK_ROOTFS:
                case DISK_VAR:
                    // Convert it from byte to megabyte
                    value = value / ( 1024 * 1024 );
                    break;
                case CPU:
                    // Convert it from nanoseconds to seconds
                    value = value / ( 1000000000 );
                    break;
                default:
                    break;
            }
            metrics.add( new HistoricalMetric( host, metricType, timestamp, value ) );
        }
    }


    @Override
    public Map<String, List<HistoricalMetric>> getHistoricalMetrics( final Collection<Host> hosts,
                                                                     final MetricType metricType )
    {
        final Map<String, List<HistoricalMetric>> historicalMetrics = new ConcurrentHashMap<>();

        for ( Host host : hosts )
        {
            try
            {
                List<HistoricalMetric> historicalMetric = getHistoricalMetric( host, metricType );
                historicalMetrics.put( historicalMetric.get( 0 ).getHost().getId(), historicalMetric );
            }
            catch ( Exception e )
            {
                //ignore
            }
        }

        // TODO enable this block as it executes the commands asynchronously
        // when agent can read them without problem and returns a response

        //        ExecutorService executor = Executors.newFixedThreadPool( hosts.size() );
        //        final Map<UUID, List<HistoricalMetric>> historicalMetrics = new ConcurrentHashMap<>();
        //        for ( final Host host : hosts ) {
        //            executor.execute( new Runnable() {
        //                @Override
        //                public void run() {
        //                    try {
        //                        List<HistoricalMetric> historicalMetric = getHistoricalMetric( host, metricType );
        //                        historicalMetrics.put( historicalMetric.get( 0 ).getHost().getId(),
        // historicalMetric );
        //                    } catch ( Exception e ) {
        //                    }
        //                }
        //            }
        //                            );
        //        }
        //        executor.shutdown();
        //        int timeout = 5;
        //        LOG.info( "Waiting for all threads to retrieve historical data for {} to finish {} seconds maximum."
        //                , metricType, timeout );
        //        // Wait until all threads are finished
        //        try {
        //            executor.awaitTermination( timeout, TimeUnit.SECONDS );
        //        } catch (InterruptedException e) {
        //            LOG.error( e.getMessage() );
        //        }
        //
        //        LOG.info( "All threads finished/timed out for retrieving {} metric. Size: {}", metricType,
        // historicalMetrics.size() );

        return historicalMetrics;
    }


    //    @Override
    private void updateHostMetric( final BaseMetric metric )
    {
        if ( metric != null )
        {
            this.metrics.put( metric.getHostId(), metric );
        }
    }

//
//    @Override
//    public BaseMetric getHostMetric( final String id )
//    {
//        return this.metrics.get( id );
//    }


//    @Override
//    public ResourceHostMetrics getResourceHostMetrics( final boolean isLocalOnly )
//    {
//        ResourceHostMetrics result = new ResourceHostMetrics();
//
//        for ( BaseMetric metric : metrics.values() )
//        {
//            if ( isLocalOnly && !metric.getPeerId().equals( peerManager.getLocalPeerInfo().getId() ) )
//            {
//                continue;
//            }
//            if ( metric instanceof ResourceHostMetric )
//            {
//                result.addMetric( ( ResourceHostMetric ) metric );
//            }
//        }
//
//        return result;
//    }
//
//
//    @Override
//    public String getHostMetricsAsHtml( final String hostId )
//    {
//        StringBuilder result = new StringBuilder();
//        BaseMetric metrics = getHostMetric( hostId );
//
//        if ( metrics instanceof ContainerHostQuotaState )
//        {
//            ContainerHostQuotaState state = ( ContainerHostQuotaState ) metrics;
//            result.append( getText( "<br>CPU : %d%%", state.getCpu() ) );
//            result.append( getText( "<br>RAM : %d%%", state.getRam() ) );
//            result.append( getText( "<br>/ : %d%%", state.getDisk().getRootfs() ) );
//            result.append( getText( "<br>/home : %d%%", state.getDisk().getHome() ) );
//            result.append( getText( "<br>/opt : %d%%", state.getDisk().getOpt() ) );
//            result.append( getText( "<br>/var : %d%%", state.getDisk().getVar() ) );
//        }
//        else
//        {
//            HostMetric hostMetric = ( HostMetric ) metrics;
//
//            result.append( getText( "<br>CPU model: %s", hostMetric.getCpuModel() ) );
//            result.append( getText( "<br>CPU core(s): %d", hostMetric.getCpuCore() ) );
//            result.append( getText( "<br>CPU load: %.2f", hostMetric.getUsedCpu() ) );
//            result.append( getText( "<br>Total RAM: %.3f Gb", hostMetric.getTotalRam() / 1024 / 1024 / 1024 ) );
//            result.append( getText( "<br>Available RAM: %.3f Gb", hostMetric.getAvailableRam() / 1024 / 1024 / 1024 ) );
//            result.append(
//                    getText( "<br>Available space: %.3f Gb", hostMetric.getAvailableSpace() / 1024 / 1024 / 1024 ) );
//
//            if ( hostMetric instanceof ResourceHostMetric )
//            {
//                result.append(
//                        getText( "<br>Container #: %d", ( ( ResourceHostMetric ) hostMetric ).getContainersCount() ) );
//            }
//        }
//        return result.toString();
//    }


//    private String getText( final String format, final Object o )
//    {
//        if ( o != null )
//        {
//            return String.format( format, o );
//        }
//        else
//        {
//            return "";
//        }
//    }


//    private class MetricsUpdater implements Runnable
//    {
//        private final MonitorImpl monitor;
//
//
//        public MetricsUpdater( final MonitorImpl monitor )
//        {
//            this.monitor = monitor;
//        }
//
//
//        @Override
//        public void run()
//        {
//            LOG.debug( "Metrics updater started." );
//            try
//            {
//
//                HostMetric managementHostMetric =
//                        monitor.fetchManagementHostMetric( peerManager.getLocalPeer().getManagementHost() );
//
//                monitor.updateHostMetric( managementHostMetric );
//
//                for ( ResourceHost resourceHost : peerManager.getLocalPeer().getResourceHosts() )
//                {
//                    ResourceHostMetric resourceHostMetric = monitor.fetchResourceHostMetric( resourceHost );
//
//                    monitor.updateHostMetric( resourceHostMetric );
//                    for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
//                    {
//                        ContainerHostQuotaState hostMetric =
//                                monitor.fetchContainerHostQuotaState( resourceHost, containerHost );
//                        monitor.updateHostMetric( hostMetric );
//                    }
//                }
//            }
//            catch ( Exception e )
//            {
//                LOG.error( e.getMessage(), e );
//            }
//            LOG.debug( "Metrics updater finished." );
//        }
//    }
}
