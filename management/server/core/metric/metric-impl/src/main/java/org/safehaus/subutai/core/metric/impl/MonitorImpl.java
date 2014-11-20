package org.safehaus.subutai.core.metric.impl;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.JsonSyntaxException;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    private static final String ENVIRONMENT_IS_NULL_MSG = "Environment is null";
    private static final String METRIC_IS_NULL_MSG = "Metric listener is null";
    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class.getName() );

    //set of metric subscribers
    protected Set<MetricListener> metricListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MetricListener, Boolean>() );
    private final Commands commands = new Commands();
    private final PeerManager peerManager;

    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MonitorDao monitorDao;


    public MonitorImpl( final DataSource dataSource, PeerManager peerManager ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );
        Preconditions.checkNotNull( peerManager, "Peer manager is null" );

        this.monitorDao = new MonitorDao( dataSource );
        this.peerManager = peerManager;
        peerManager.addRequestListener( new RemoteAlertListener( this ) );
        peerManager.addRequestListener( new RemoteMetricRequestListener( this ) );
    }


    @Override
    public Set<ContainerHostMetric> getContainerMetrics( final Environment environment ) throws MonitorException
    {
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );

        Set<ContainerHostMetric> metrics = new HashSet<>();

        //obtain environment containers
        Set<ContainerHost> containerHosts = environment.getContainers();

        Set<Peer> peers = Sets.newHashSet();

        //determine container peers
        for ( ContainerHost containerHost : containerHosts )
        {
            try
            {
                peers.add( containerHost.getPeer() );
            }
            catch ( PeerException e )
            {
                LOG.warn( String.format( "Could not obtain peer for container %s", containerHost.getHostname() ), e );
            }
        }

        //send metric requests to target peers
        for ( Peer peer : peers )
        {
            if ( peer.isLocal() )
            {
                //dispatch locally
                metrics.addAll( getLocalContainerHostMetrics( environment.getId() ) );
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
                            Constants.METRIC_REQUEST_TIMEOUT, ContainerHostMetricResponse.class );

            //if response contains metrics, add them to result
            if ( response != null && !CollectionUtil.isCollectionEmpty( response.getMetrics() ) )
            {
                metrics.addAll( response.getMetrics() );
            }
        }
        catch ( PeerException e )
        {
            LOG.warn( String.format( "Error obtaining metrics from peer %s", peer.getName() ), e );
        }
        return metrics;
    }


    protected Set<ContainerHostMetricImpl> getLocalContainerHostMetrics( UUID environmentId )
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
                getContainerMetrics( environmentId, resourceHost, localContainer, metrics );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error in getLocalContainerHostMetrics", e );
        }
        return metrics;
    }


    protected void getContainerMetrics( final UUID environmentId, final ResourceHost resourceHost,
                                        final ContainerHost localContainer, Set<ContainerHostMetricImpl> metrics )
    {
        if ( resourceHost != null )
        {
            try
            {
                //execute metrics command
                CommandResult result = resourceHost
                        .execute( commands.getReadContainerHostMetricCommand( localContainer.getHostname() ) );
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
                LOG.error( "Error in getContainerMetrics", e );
            }
        }
        else
        {
            LOG.warn( String.format( "Could not find resource host %s", localContainer.getParentHostname() ) );
        }
    }


    @Override
    public Set<ResourceHostMetric> getResourceHostMetrics() throws MonitorException
    {
        Set<ResourceHostMetric> metrics = new HashSet<>();
        //obtain resource hosts
        Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();
        //iterate resource hosts and get their metrics
        for ( ResourceHost resourceHost : resourceHosts )
        {
            getResourceMetrics( resourceHost, metrics );
        }


        return metrics;
    }


    protected void getResourceMetrics( ResourceHost resourceHost, Set<ResourceHostMetric> metrics )
    {
        try
        {
            CommandResult result = resourceHost.execute( commands.getReadResourceHostMetricCommand() );
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
            LOG.error( "Error in getResourceMetrics", e );
        }
    }


    @Override
    public void startMonitoring( final MetricListener metricListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, METRIC_IS_NULL_MSG );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
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
    }


    @Override
    public void stopMonitoring( final MetricListener metricListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, METRIC_IS_NULL_MSG );
        Preconditions.checkNotNull( environment, ENVIRONMENT_IS_NULL_MSG );
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
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


    /**
     * This method is called by REST endpoint from local peer indicating that some container hosted locally is under
     * stress.
     *
     * @param alertMetric - body of alert in JSON
     */
    @Override
    public void alertThresholdExcess( final String alertMetric ) throws MonitorException
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

                //find container's owner peer
                Peer ownerPeer = peerManager.getPeer( UUID.fromString( containerHost.getCreatorPeerId() ) );

                //if container is "owned" by local peer, alert local peer
                if ( ownerPeer.isLocal() )
                {
                    alertThresholdExcess( containerHostMetric );
                }
                //send metric to owner peer
                else
                {
                    ownerPeer.sendRequest( containerHostMetric, RecipientType.ALERT_RECIPIENT.name(),
                            Constants.ALERT_TIMEOUT );
                }
            }
        }
        catch ( PeerException | JsonSyntaxException e )
        {
            LOG.error( "Error in alertThresholdExcess", e );
            throw new MonitorException( e );
        }
    }


    /**
     * This methods is called by REST endpoint when a remote peer sends an alert from one of its hosted containers
     * belonging to this peer or when local "own" container is under stress
     *
     * @param metric - {@code ContainerHostMetric} metric of the host where thresholds are being exceeded
     */
    public void alertThresholdExcess( final ContainerHostMetric metric ) throws MonitorException
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
            LOG.error( "Error in alertThresholdExcess", e );
            throw new MonitorException( e );
        }
    }


    protected void notifyListener( final ContainerHostMetric metric, String subscriberId )
    {
        for ( final MetricListener listener : metricListeners )
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
    public void addMetricListener( MetricListener metricListener )
    {
        if ( metricListener != null )
        {
            metricListeners.add( metricListener );
        }
    }


    @Override
    public void removeMetricListener( MetricListener metricListener )
    {
        if ( metricListener != null )
        {
            metricListeners.remove( metricListener );
        }
    }
}
