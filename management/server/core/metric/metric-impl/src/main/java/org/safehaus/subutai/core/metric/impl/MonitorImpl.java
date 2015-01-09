package org.safehaus.subutai.core.metric.impl;


import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.AlertListener;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.MonitoringSettings;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.HostNotFoundException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    private static final String ENVIRONMENT_IS_NULL_MSG = "Environment is null";
    private static final String CONTAINER_IS_NULL_MSG = "Container is null";
    private static final String ALERT_LISTENER_IS_NULL = "Alert listener is null";
    private static final String SETTINGS_IS_NULL_MSG = "Settings is null";
    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class.getName() );

    //set of metric subscribers
    protected Set<AlertListener> alertListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<AlertListener, Boolean>() );
    private final Commands commands = new Commands();
    private final PeerManager peerManager;

    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MonitorDao monitorDao;


    public MonitorImpl( PeerManager peerManager, EntityManagerFactory emf ) throws MonitorException
    {
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );
        Preconditions.checkNotNull( emf, "EntityManagerFactory is null." );
        try
        {
            this.monitorDao = new MonitorDao( emf );
            this.peerManager = peerManager;
            peerManager.addRequestListener( new RemoteAlertListener( this ) );
            peerManager.addRequestListener( new RemoteMetricRequestListener( this ) );
            peerManager.addRequestListener( new MonitoringActivationListener( this, peerManager ) );
        }
        catch ( DaoException e )
        {
            throw new MonitorException( e );
        }
    }


    @Override
    public Set<ContainerHostMetric> getContainerHostsMetrics( final Environment environment ) throws MonitorException
    {
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );

        Set<ContainerHostMetric> metrics = new HashSet<>();

        //obtain environment containers
        Set<ContainerHost> containerHosts = environment.getContainerHosts();

        Set<Peer> peers = Sets.newHashSet();

        //determine container peers
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

        //send metric requests to target peers
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


    protected Set<ContainerHostMetricImpl> getRemoteContainerHostsMetrics( UUID environmentId, Peer peer )
    {
        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();
        try
        {
            //create request for metrics
            ContainerHostMetricRequest request = new ContainerHostMetricRequest( environmentId );

            //send request and obtain metrics
            ContainerHostMetricResponse response =
                    peer.sendRequest( request, RecipientType.METRIC_REQUEST_RECIPIENT.name(),
                            Constants.METRIC_REQUEST_TIMEOUT, ContainerHostMetricResponse.class,
                            Constants.METRIC_REQUEST_TIMEOUT );

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


    protected Set<ContainerHostMetricImpl> getLocalContainerHostsMetrics( UUID environmentId )
    {

        Set<ContainerHostMetricImpl> metrics = Sets.newHashSet();
        try
        {
            //obtain environment containers
            Set<ContainerHost> localContainers =
                    peerManager.getLocalPeer().getContainerHostsByEnvironmentId( environmentId );

            for ( ContainerHost localContainer : localContainers )
            {
                //get container's resource host
                ResourceHost resourceHost =
                        peerManager.getLocalPeer().getResourceHostByName( localContainer.getParentHostname() );
                addLocalContainerHostMetric( environmentId, resourceHost, localContainer, metrics );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error obtaining local container metrics", e );
        }
        return metrics;
    }


    protected void addLocalContainerHostMetric( final UUID environmentId, final ResourceHost resourceHost,
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
                    metrics.add( metric );
                }
                else
                {
                    LOG.warn( String.format( "Error getting metrics from %s: %s", localContainer.getHostname(),
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
            LOG.warn( String.format( "Could not find resource host %s", localContainer.getParentHostname() ) );
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


    protected void addResourceHostMetric( ResourceHost resourceHost, Set<ResourceHostMetric> metrics )
    {
        try
        {
            CommandResult result =
                    resourceHost.execute( commands.getCurrentMetricCommand( resourceHost.getHostname() ) );
            if ( result.hasSucceeded() )
            {
                ResourceHostMetricImpl metric = JsonUtil.fromJson( result.getStdOut(), ResourceHostMetricImpl.class );
                //set peer id for future reference
                metric.setPeerId( peerManager.getLocalPeer().getId() );
                metrics.add( metric );
            }
            else
            {
                LOG.warn( String.format( "Error getting metrics from %s: %s", resourceHost.getHostname(),
                        result.getStdErr() ) );
            }
        }
        catch ( CommandException | JsonSyntaxException e )
        {
            LOG.error( "Error in addResourceHostMetric", e );
        }
    }


    @Override
    public void startMonitoring( final AlertListener alertListener, final Environment environment,
                                 final MonitoringSettings monitoringSettings ) throws MonitorException
    {
        Preconditions.checkNotNull( alertListener, ALERT_LISTENER_IS_NULL );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );
        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );


        //make sure subscriber id is truncated to 100 characters
        String subscriberId = alertListener.getSubscriberId();
        if ( subscriberId.length() > Constants.MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, Constants.MAX_SUBSCRIBER_ID_LEN );
        }
        //save subscription to database
        try
        {
            monitorDao.addSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in startMonitoring", e );
            throw new MonitorException( e );
        }

        //activate monitoring
        activateMonitoring( environment.getContainerHosts(), monitoringSettings );
    }


    @Override
    public void stopMonitoring( final AlertListener alertListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( alertListener, ALERT_LISTENER_IS_NULL );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = alertListener.getSubscriberId();
        if ( subscriberId.length() > Constants.MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, Constants.MAX_SUBSCRIBER_ID_LEN );
        }
        //remove subscription from database
        try
        {
            monitorDao.removeSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in stopMonitoring", e );
            throw new MonitorException( e );
        }
    }


    @Override
    public void activateMonitoring( final ContainerHost containerHost, final MonitoringSettings monitoringSettings )
            throws MonitorException

    {
        Preconditions.checkNotNull( containerHost, CONTAINER_IS_NULL_MSG );
        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );

        activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings );
    }


    protected void activateMonitoring( Set<ContainerHost> containerHosts, MonitoringSettings monitoringSettings )
            throws MonitorException
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
                activateMonitoringAtRemoteContainers( peer, containers, monitoringSettings );
            }
        }
    }


    protected void activateMonitoringAtRemoteContainers( Peer peer, Set<ContainerHost> containerHosts,
                                                         MonitoringSettings monitoringSettings )
    {
        Preconditions.checkNotNull( peer );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );

        try
        {
            peer.sendRequest( new MonitoringActivationRequest( containerHosts, monitoringSettings ),
                    RecipientType.MONITORING_ACTIVATION_RECIPIENT.name(), Constants.MONITORING_ACTIVATION_TIMEOUT );
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
                        peerManager.getLocalPeer().getResourceHostByName( containerHost.getParentHostname() );
                CommandResult commandResult = resourceHost.execute(
                        commands.getActivateMonitoringCommand( containerHost.getHostname(), monitoringSettings ) );
                if ( !commandResult.hasSucceeded() )
                {
                    LOG.warn( String.format( "Error activating metrics on %s: %s %s", containerHost.getHostname(),
                            commandResult.getStatus(), commandResult.getStdErr() ) );
                }
            }
            catch ( CommandException | PeerException e )
            {
                LOG.error( "Error in activateMonitoringAtLocalContainers", e );
            }
        }
    }


    @Override
    public ProcessResourceUsage getProcessResourceUsage( final ContainerHost containerHost, final int processPid )
            throws MonitorException
    {
        Preconditions.checkNotNull( containerHost );
        Preconditions.checkArgument( processPid > 0 );
        try
        {
            ResourceHost resourceHost =
                    peerManager.getLocalPeer().getResourceHostByContainerName( containerHost.getHostname() );
            CommandResult commandResult = resourceHost
                    .execute( commands.getProcessResourceUsageCommand( containerHost.getHostname(), processPid ) );
            if ( !commandResult.hasSucceeded() )
            {
                throw new MonitorException(
                        String.format( "Error getting process resource usage of pid=%d on %s: %s %s", processPid,
                                containerHost.getHostname(), commandResult.getStatus(), commandResult.getStdErr() ) );
            }
            //TODO actualize output of the command
            return JsonUtil.fromJson( commandResult.getStdOut(), ProcessResourceUsage.class );
        }
        catch ( CommandException | HostNotFoundException | JsonSyntaxException e )
        {
            LOG.error( String.format( "Could not obtain process resource usage for container %s, pid %d",
                    containerHost.getHostname(), processPid ), e );
            throw new MonitorException( e );
        }
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
            //find associated container host
            ContainerHost containerHost =
                    peerManager.getLocalPeer().getContainerHostByName( containerHostMetric.getHost() );
            if ( containerHost != null )
            {
                //set metric's environment id for future reference on the receiving end
                containerHostMetric.setEnvironmentId( UUID.fromString( containerHost.getEnvironmentId() ) );

                //find container's creator peer
                Peer creatorPeer = peerManager.getPeer( UUID.fromString( containerHost.getCreatorPeerId() ) );

                //if container is "created" by local peer, notifyOnAlert local peer
                if ( creatorPeer.isLocal() )
                {
                    notifyOnAlert( containerHostMetric );
                }
                //send metric to remote creator peer
                else
                {
                    creatorPeer.sendRequest( containerHostMetric, RecipientType.ALERT_RECIPIENT.name(),
                            Constants.ALERT_TIMEOUT );
                }
            }
        }
        catch ( PeerException | JsonSyntaxException e )
        {
            LOG.error( "Error in onAlert", e );
            throw new MonitorException( e );
        }
    }


    /**
     * This methods is called by REST endpoint when a remote peer sends an notifyOnAlert from one of its hosted
     * containers belonging to this peer or when local "own" container is under stress
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void notifyOnAlert( final ContainerHostMetric metric ) throws MonitorException
    {
        try
        {
            //search for environment, if not found then no-op
            Set<String> subscribersIds = monitorDao.getEnvironmentSubscribersIds( metric.getEnvironmentId() );
            //search for subscriber if not found then no-op
            for ( String subscriberId : subscribersIds )
            {
                //notify subscriber on alert
                notifyListener( metric, subscriberId );
            }
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in notifyOnAlert", e );
            throw new MonitorException( e );
        }
    }


    protected void notifyListener( final ContainerHostMetric metric, String subscriberId )
    {
        for ( final AlertListener listener : alertListeners )
        {
            if ( subscriberId.equalsIgnoreCase( listener.getSubscriberId() ) )
            {
                notificationExecutor.execute( new AlertNotifier( metric, listener ) );

                return;
            }
        }
    }


    public void destroy()
    {
        notificationExecutor.shutdown();
    }


    @Override
    public void addAlertListener( AlertListener alertListener )
    {
        Preconditions.checkNotNull( alertListener );

        alertListeners.add( alertListener );
    }


    @Override
    public void removeAlertListener( AlertListener alertListener )
    {
        Preconditions.checkNotNull( alertListener );

        alertListeners.remove( alertListener );
    }
}
