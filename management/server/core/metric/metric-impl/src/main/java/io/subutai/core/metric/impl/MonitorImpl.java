package io.subutai.core.metric.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.DaoException;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.Alert;
import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.QuotaAlert;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertEvent;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.pojo.P2PInfo;
import io.subutai.core.metric.impl.pojo.P2PInfoPojo;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Monitor
 */
public class MonitorImpl extends HostListener implements Monitor
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class );
    private static final int ALERT_LIVE_TIME = 2;// alert live time in min
    private final HostRegistry hostRegistry;

    protected Set<AlertListener> alertListeners =
            Collections.newSetFromMap( new ConcurrentHashMap<AlertListener, Boolean>() );

    private final Commands commands = new Commands();
    protected MonitorDataService monitorDataService;
    protected DaoManager daoManager;

    protected ScheduledExecutorService backgroundTasksExecutorService;
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();

    private Map<String, AlertEvent> alertQueue = new HashMap<>();

    private List<AlertEvent> alerts = new CopyOnWriteArrayList<>();

    private PeerManager peerManager;
    protected ObjectMapper mapper = new ObjectMapper();


    public MonitorImpl( PeerManager peerManager, DaoManager daoManager, HostRegistry hostRegistry )
            throws MonitorException
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkNotNull( hostRegistry );

        try
        {
            this.daoManager = daoManager;
            this.monitorDataService = new MonitorDataService( daoManager.getEntityManagerFactory() );
            this.peerManager = peerManager;
            this.hostRegistry = hostRegistry;
        }
        catch ( DaoException e )
        {
            throw new MonitorException( e );
        }

        backgroundTasksExecutorService = Executors.newSingleThreadScheduledExecutor();
        backgroundTasksExecutorService.scheduleWithFixedDelay( new BackgroundTasksRunner(), 10, 30, TimeUnit.SECONDS );
    }


    public void addAlertListener( AlertListener alertListener )
    {
        if ( alertListener != null && alertListener.getId() != null && !alertListener.getId().trim().isEmpty() )
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
    public Set<AlertListener> getAlertListeners()
    {
        return alertListeners;
    }


    private ResourceHostMetric fetchResourceHostMetric( ResourceHost resourceHost )
    {
        ResourceHostMetric result = null;
        try
        {

            CommandResult commandResult = resourceHost.execute( commands.getRhMetricCommand() );
            if ( commandResult.hasSucceeded() )
            {
                result = JsonUtil.fromJson( commandResult.getStdOut(), ResourceHostMetric.class );
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


    public void destroy()
    {
        backgroundTasksExecutorService.shutdown();
        notificationExecutor.shutdown();
    }


    @Override
    public String getHistoricalMetrics( final Host host, final Date startTime, final Date endTime )
    {

        String result = null;

        try
        {

            CommandResult commandResult;

            RequestBuilder historicalMetricCommand = commands.getHistoricalMetricCommand( host, startTime, endTime );

            if ( host instanceof ResourceHost )
            {
                commandResult = peerManager.getLocalPeer().getResourceHostById( host.getId() )
                                           .execute( historicalMetricCommand );
            }
            else if ( host instanceof ContainerHost )
            {

                commandResult = peerManager.getLocalPeer().getResourceHostByContainerId( host.getId() )
                                           .execute( historicalMetricCommand );
            }
            else
            {
                commandResult = peerManager.getLocalPeer().getManagementHost().execute( historicalMetricCommand );
            }

            if ( commandResult.hasSucceeded() )
            {
                result = commandResult.getStdOut();
            }
            else
            {
                LOG.error( String.format( "Error getting historical metrics from %s: %s", host.getHostname(),
                        commandResult.getStdErr() ) );
            }
        }
        catch ( CommandException e )
        {
            LOG.error( "Could not run command successfully! Error: {}", e.getMessage() );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( "Could not find resource host of host {}!", host.getHostname() );
        }


        return result;
    }


    @Override
    public void addAlert( final AlertEvent alert )
    {
        AlertEvent a = new AlertEvent( alert.getPeerId(), alert.getEnvironmentId(), alert.getContainerId(),
                alert.getTemplateName(), alert.getResource(), alert.getExpiredTime() );
        LOG.debug( "Accepted new alert: " + a );
        alerts.add( a );
    }


    protected void queueAlertResource( Alert alert )
    {
        if ( alert == null || !alert.validate() )
        {
            // skipping invalid alert
            return;
        }

        AlertEvent alertEvent = alertQueue.get( alert.getId() );

        if ( alertEvent != null && !alertEvent.isExpired() )
        {
            // skipping, alert already exists
            LOG.debug( String.format( "Alert already in queue. %s", alertEvent ) );

            return;
        }

        alertEvent = buildAlertPack( alert );
        alertQueue.put( alert.getId(), alertEvent );
    }


    private AlertEvent buildAlertPack( Alert alert )
    {
        String containerId = alert.getHostId().getId();
        AlertEvent packet = null;
        try
        {
            ContainerHost host = peerManager.getLocalPeer().getContainerHostById( containerId );
            packet = new AlertEvent( host.getInitiatorPeerId(), host.getEnvironmentId().getId(), containerId,
                    host.getTemplateName(), alert, buildExpireTime().getTime() );
        }
        catch ( HostNotFoundException e )
        {
            LOG.warn( e.getMessage() );
        }

        return packet;
    }


    protected Date buildExpireTime()
    {
        return DateUtils.addMinutes( new Date(), ALERT_LIVE_TIME );
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        ResourceHostMetrics result = new ResourceHostMetrics();

        Set<ResourceHost> resourceHosts = peerManager.getLocalPeer().getResourceHosts();

        result.setResourceHostCount( resourceHosts.size() );

        for ( ResourceHost resourceHost : resourceHosts )
        {
            final ResourceHostMetric m = getResourceHostMetric( resourceHost );
            if ( m != null )
            {
                result.addMetric( m );
            }
        }

        return result;
    }


    @Override
    public ResourceHostMetric getResourceHostMetric( final ResourceHost resourceHost )
    {
        ResourceHostMetric resourceHostMetric = null;

        try
        {
            ResourceHostInfo hostInfo = hostRegistry.getResourceHostInfoById( resourceHost.getId() );
            ResourceHostInfoModel resourceHostInfoModel = new ResourceHostInfoModel( hostInfo );
            resourceHostMetric = new ResourceHostMetric( peerManager.getLocalPeer().getId(), resourceHostInfoModel );
            resourceHostMetric.setManagement( resourceHost.isManagementHost() );
            resourceHostMetric.setConnected( true );

            ResourceHostMetric m = fetchResourceHostMetric( resourceHost );
            if ( m != null )
            {
                resourceHostMetric.updateMetrics( m );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }

        return resourceHostMetric;
    }


    @Override
    public Collection<AlertEvent> getAlertEvents()
    {
        return Collections.unmodifiableCollection( alerts );
    }


    @Override
    public List<AlertEvent> getAlertsQueue()
    {
        List<AlertEvent> result = new ArrayList<>();
        result.addAll( alertQueue.values() );
        return result;
    }


    protected void notifyAlertListeners()
    {
        for ( AlertEvent alertEvent : alerts )
        {
            if ( !alertEvent.isDelivered() && !alertEvent.isExpired() )
            {
                for ( AlertListener alertListener : alertListeners )
                {
                    AlertNotifier alertNotifier = new AlertNotifier( alertEvent, alertListener );
                    notificationExecutor.submit( alertNotifier );
                }
                alertEvent.setDelivered( true );
            }
        }
    }


    //    @Override
    protected void deliverAlerts()
    {
        for ( AlertEvent alertEvent : alertQueue.values() )
        {
            if ( !alertEvent.isDelivered() )
            {
                deliverAlertPackToPeer( alertEvent );
            }
        }
    }


    private void clearObsoleteAlerts()
    {
        for ( AlertEvent alertEvent : alertQueue.values() )
        {
            if ( alertEvent.isExpired() )
            {
                LOG.debug( String.format( "Alert package '%s' expired. ", alertEvent.getResource().getId() ) );
                // removing obsolete alert
                alertQueue.remove( alertEvent.getResource().getId() );
            }
        }
    }


    private void deliverAlertPackToPeer( final AlertEvent alertEvent )
    {
        try
        {
            Peer peer = peerManager.getPeer( alertEvent.getPeerId() );
            new AlertDeliver( peer, alertEvent ).run();
        }
        catch ( PeerException e )
        {
            LOG.warn( String.format( "Destination peer '%s' for alert '%s' not found.", alertEvent.getPeerId(),
                    alertEvent.getResource().getId() ) );
        }
    }


    private class BackgroundTasksRunner implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                deliverAlerts();
                notifyAlertListeners();
                clearObsoleteAlerts();
            }
            catch ( Exception e )
            {
                LOG.warn( "Background task execution failed: " + e.getMessage() );
            }
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
        if ( alerts != null )
        {
            for ( QuotaAlertValue quotaAlertValue : alerts )
            {
                final ResourceHostMetric metrics;
                try
                {
                    metrics = getResourceHostMetric(
                            peerManager.getLocalPeer().getResourceHostById( resourceHostInfo.getId() ) );
                    quotaAlertValue.getValue().setResourceHostMetric( metrics );
                }
                catch ( PeerException e )
                {
                    LOG.warn( e.getMessage() );
                }
                queueAlertResource( new QuotaAlert( quotaAlertValue, System.currentTimeMillis() ) );
            }
        }
    }


    @Override
    public void putAlert( final Alert alert )
    {
        queueAlertResource( alert );
    }


    @Override
    public List<P2PInfo> getP2PStatuses()
    {
        List<P2PInfo> pojos = Lists.newArrayList();

        for ( final ResourceHost resourceHost : peerManager.getLocalPeer().getResourceHosts() )
        {

            try
            {
                P2PInfo info = getP2pStatus( resourceHost.getId() );

                pojos.add( info );
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting P2P status: {}", e.getMessage() );
            }
        }

        return pojos;
    }


    @Override
    public P2PInfo getP2pStatus( String rhId ) throws Exception
    {
        P2PInfoPojo info = new P2PInfoPojo();

        List<String> statusLines = Lists.newArrayList();

        ResourceHost resourceHost = peerManager.getLocalPeer().getResourceHostById( rhId );

        CommandResult result = resourceHost.execute( new RequestBuilder( "p2p status" ) );
        String status = result.getStdOut();

        String lines[] = status.split( "\\r?\\n" );

        for ( String line : lines )
        {
            if ( StringUtils.isNotBlank( line.trim() ) )
            {
                statusLines.add( line );
            }
        }

        info.setRhId( resourceHost.getId() );
        info.setState( statusLines );
        info.setP2pStatus( result.getExitCode() );

        return info;
    }


    @Override
    public HistoricalMetrics getMetricsSeries( final Host host, Date startTime, Date endTime )
    {
        HistoricalMetrics result = new HistoricalMetrics( startTime, endTime );

        try
        {
            CommandResult commandResult = getHistoricalMetricsResp( host, startTime, endTime );


            if ( null != commandResult && commandResult.hasSucceeded() )
            {
                LOG.debug( "OBTAINED METRIC: \n {}", commandResult.getStdOut() );
                result = mapper.readValue( commandResult.getStdOut(), HistoricalMetrics.class );
                result.setStartTime( startTime );
                result.setEndTime( endTime );
            }
            else
            {
                LOG.error( String.format( "Error getting historical metrics from %s: %s", host.getHostname(),
                        commandResult != null ? commandResult.getStdErr() : "" ) );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Could not run command successfully! Error: {}", e.getMessage() );
        }


        return result;
    }


    private CommandResult getHistoricalMetricsResp( final Host host, final Date startTime, final Date endTime )
            throws CommandException, HostNotFoundException
    {
        Preconditions.checkNotNull( host );

        RequestBuilder historicalMetricCommand = commands.getHistoricalMetricCommand( host, startTime, endTime );

        CommandResult commandResult;
        if ( host instanceof ResourceHost )
        {
            commandResult =
                    peerManager.getLocalPeer().getResourceHostById( host.getId() ).execute( historicalMetricCommand );
        }
        else if ( host instanceof ContainerHost )
        {
            commandResult = peerManager.getLocalPeer().getResourceHostByContainerId( host.getId() )
                                       .execute( historicalMetricCommand );
        }
        else
        {
            commandResult = peerManager.getLocalPeer().getManagementHost().execute( historicalMetricCommand );
        }

        return commandResult;
    }
}
