package io.subutai.core.metric.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.DaoException;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInfoModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.AlertResource;
import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.QuotaAlertResource;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertListener;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor, HostListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class );

    private static final int METRICS_UPDATE_DELAY = 60;
    private static final int ALERT_LIVE_TIME = 2;// alert live time in min
    private final HostRegistry hostRegistry;

    //    protected Set<AlertListener> alertListeners =
    //            Collections.newSetFromMap( new ConcurrentHashMap<AlertListener, Boolean>() );
    protected Map<String, AlertListener> alertListeners = new ConcurrentHashMap<>();

    private final Commands commands = new Commands();
    private final EnvironmentManager environmentManager;
    //    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MonitorDao monitorDao;
    protected DaoManager daoManager;
    private Cache<String, BaseMetric> metrics = CacheBuilder.newBuilder().
            expireAfterWrite( METRICS_UPDATE_DELAY * 2, TimeUnit.SECONDS ).
                                                                    build();
    protected ScheduledExecutorService stateUpdateExecutorService;
    protected ScheduledExecutorService backgroundTasksExecutorService;

    private Map<String, AlertPack> localALerts = new HashMap<>();
    private List<AlertPack> alerts = new CopyOnWriteArrayList<>();

    private PeerManager peerManager;
    //    private AlertProcessor alertProcessor = new AlertProcessor();
    protected ObjectMapper mapper = new ObjectMapper();


    public MonitorImpl( PeerManager peerManager, DaoManager daoManager, EnvironmentManager environmentManager,
                        HostRegistry hostRegistry ) throws MonitorException
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( daoManager );
        Preconditions.checkNotNull( environmentManager );
        Preconditions.checkNotNull( hostRegistry );

        try
        {
            this.daoManager = daoManager;
            this.monitorDao = new MonitorDao( daoManager.getEntityManagerFactory() );
            this.peerManager = peerManager;
            this.environmentManager = environmentManager;
            this.hostRegistry = hostRegistry;
        }
        catch ( DaoException e )
        {
            throw new MonitorException( e );
        }

        stateUpdateExecutorService = Executors.newScheduledThreadPool( 1 );
        stateUpdateExecutorService
                .scheduleWithFixedDelay( new MetricsUpdater( this ), 10, METRICS_UPDATE_DELAY, TimeUnit.SECONDS );
        backgroundTasksExecutorService = Executors.newScheduledThreadPool( 1 );
        backgroundTasksExecutorService.scheduleWithFixedDelay( new BackgroundTasksRunner(), 10, 30, TimeUnit.SECONDS );
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

                //                result = new ResourceHostMetric( peerManager.getLocalPeer().getId(), null );
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
            result = new ResourceHostMetric( resourceHost.getPeerId() );
        }

        return result;
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


    public void destroy()
    {
        backgroundTasksExecutorService.shutdown();
        stateUpdateExecutorService.shutdown();
    }


    @Override
    public HistoricalMetrics getHistoricalMetrics( final Host host, Date startTime, Date endTime )
    {
        Preconditions.checkNotNull( host );

        HistoricalMetrics result = new HistoricalMetrics();

        try
        {
            RequestBuilder historicalMetricCommand = commands.getHistoricalMetricCommand( host, startTime, endTime );

            CommandResult commandResult;
            if( host instanceof ResourceHost )
            {
                commandResult =
                        peerManager.getLocalPeer().getResourceHostById( host.getId() ).execute( historicalMetricCommand );
            }
            else
            {
                commandResult =
                        peerManager.getLocalPeer().getManagementHost().execute( historicalMetricCommand );
            }


            if ( null != commandResult && commandResult.hasSucceeded() )
            {
                result = mapper.readValue( commandResult.getStdOut(), HistoricalMetrics.class );
            }
            else
            {
                LOG.error( String.format( "Error getting historical metrics from %s: %s", host.getHostname(),
                        commandResult.getStdErr() ) );
            }
        }
        catch ( IOException | CommandException e )
        {
            LOG.error( "Could not run command successfully! Error: {}", e );
        }
        catch ( HostNotFoundException e )
        {
            LOG.error( "Could not find resource host of host {}!", host.getHostname() );
        }


        return result;
    }


    @Override
    public void addAlert( final AlertPack alert )
    {
        AlertPack a = new AlertPack( alert.getPeerId(), alert.getEnvironmentId(), alert.getContainerId(),
                alert.getTemplateName(), alert.getResource(), alert.getExpiredTime() );
        LOG.debug( "Accepted new alert: " + a );
        alerts.add( a );
    }


    protected void queueAlertResource( AlertResource alert )
    {

        //        if ( !isValidAlert( alert ) )
        //        {
        //            return;
        //        }


        AlertPack alertPack = localALerts.get( alert.getId() );
        if ( alertPack != null )
        {
            if ( !alertPack.isExpired() )
            {
                // skipping, alert already exists
                LOG.debug( String.format( "Alert already in queue. %s", alertPack ) );
                return;
            }
        }

        alertPack = buildAlertPack( alert );
        localALerts.put( alert.getId(), alertPack );
    }


    private AlertPack buildAlertPack( final AlertResource alert )
    {
        String containerId = alert.getHostId().getId();
        AlertPack packet = null;
        try
        {
            ContainerHost host = peerManager.getLocalPeer().getContainerHostById( containerId );
            packet = new AlertPack( host.getInitiatorPeerId(), host.getEnvironmentId().getId(), containerId,
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


    //
    //    private boolean isValidAlert( final Alert alertValue )
    //    {
    //        try
    //        {
    //            Preconditions.checkNotNull( alertValue, "Alert value is null" );
    //            Preconditions.checkNotNull( alertValue.getHostId(), "Host id is null" );
    //            Preconditions.checkNotNull( alertValue.getHostId().getId(), "Host id is null" );
    //            return true;
    //        }
    //        catch ( Exception e )
    //        {
    //            LOG.warn( "Invalid alert value: " + e.getMessage() );
    //            return false;
    //        }
    //    }


    private boolean isValidResourceAlert( final ResourceAlert alertValue )
    {
        try
        {
            Preconditions.checkNotNull( alertValue, "Alert value is null" );
            Preconditions.checkNotNull( alertValue.getHostId(), "Host id is null" );
            Preconditions.checkNotNull( alertValue.getHostId().getId(), "Host id is null" );
            Preconditions.checkNotNull( alertValue.getResourceType(), "Resource type is null" );
            Preconditions.checkNotNull( alertValue.getCurrentValue(), "Current value is null" );
            Preconditions.checkNotNull( alertValue.getQuotaValue(), "Quota value is null" );
            return true;
        }
        catch ( Exception e )
        {
            LOG.warn( "Invalid alert value: " + e.getMessage() );
            return false;
        }
    }


    private void updateHostMetric( final BaseMetric metric )
    {
        if ( metric != null )
        {
            this.metrics.put( metric.getHostInfo().getId(), metric );
        }
    }


    @Override
    public BaseMetric getHostMetric( final String id )
    {
        return this.metrics.getIfPresent( id );
    }


    @Override
    public Collection<BaseMetric> getMetrics()
    {
        return Collections.unmodifiableCollection( metrics.asMap().values() );
    }


    @Override
    public ResourceHostMetrics getResourceHostMetrics()
    {
        ResourceHostMetrics result = new ResourceHostMetrics();

        for ( BaseMetric baseMetric : getMetrics() )
        {
            if ( baseMetric instanceof ResourceHostMetric && !"management"
                    .equals( ( ( ResourceHostMetric ) baseMetric ).getHostName() ) )
            {
                result.addMetric( ( ResourceHostMetric ) baseMetric );
            }
        }

        return result;
    }


    @Override
    public Collection<AlertPack> getAlerts()
    {
        return Collections.unmodifiableCollection( alerts );
    }


    @Override
    public void addAlertListener( final AlertListener alertListener )
    {
        if ( alertListener != null && alertListener.getTemplateName() != null )
        {
            this.alertListeners.put( alertListener.getTemplateName(), alertListener );
        }
    }


    @Override
    public void removeAlertListener( final AlertListener alertListener )
    {
        if ( alertListener != null )
        {
            this.alertListeners.remove( alertListener.getTemplateName() );
        }
    }


    @Override
    public Collection<AlertListener> getAlertListeners()
    {
        return alertListeners.values();
    }


    @Override
    public List<AlertPack> getAlertPackages()
    {
        return alerts;
    }


    @Override
    public List<AlertPack> getAlertsQueue()
    {
        List<AlertPack> result = new ArrayList<>();
        result.addAll( localALerts.values() );
        return result;
    }


    @Override
    public void notifyAlertListeners()
    {
        for ( AlertPack alertPack : alerts )
        {
            LOG.debug( "Notifying: " + alertPack );
            if ( !alertPack.isDelivered() && !alertPack.isExpired() )
            {
                notifyListener( alertPack );
                alertPack.setDelivered( true );
            }
        }
    }


    protected void notifyListener( final AlertPack alertPack )
    {
        AlertListener alertListener = alertListeners.get( alertPack.getTemplateName() );

        if ( alertListener != null )
        {
            alertPack.setDelivered( true );
            AlertNotifier alertNotifier = new AlertNotifier( alertPack, alertListener );
            alertNotifier.run();
        }
        else
        {
            LOG.warn( String.format( "Alert handler not found for: %s", alertPack ) );
        }
    }


    @Override
    public void deliverAlerts()
    {
        for ( AlertPack alertPack : localALerts.values() )
        {
            if ( !alertPack.isDelivered() )
            {
                deliverAlert( alertPack );
            }
        }
    }


    private void clearObsoleteAlerts()
    {
        for ( AlertPack alertPack : localALerts.values() )
        {
            if ( alertPack.isExpired() )
            {
                LOG.debug( String.format( "Alert package '%s' expired. ", alertPack.getResource().getId() ) );
                // removing obsolete alert
                localALerts.remove( alertPack.getResource().getId() );
            }
        }
    }


    private void deliverAlert( final AlertPack alertPack )
    {
        Peer peer = peerManager.getPeer( alertPack.getPeerId() );
        if ( peer != null )
        {
            new AlertDeliver( peer, alertPack ).run();
        }
        else
        {
            LOG.warn( String.format( "Destination peer '%s' for alert '%s' not found.", alertPack.getPeerId(),
                    alertPack.getResource().getId() ) );
        }
    }


    private class BackgroundTasksRunner implements Runnable
    {
        @Override
        public void run()
        {
            LOG.debug( "Background task runner started..." );
            try
            {
                deliverAlerts();
                notifyAlertListeners();
                clearObsoleteAlerts();
            }
            catch ( Exception e )
            {
                LOG.warn( "Background task execution faild: " + e.getMessage() );
            }
            LOG.debug( "Background task runner finished." );
        }
    }


    private class MetricsUpdater implements Runnable
    {
        private final MonitorImpl monitor;


        public MetricsUpdater( final MonitorImpl monitor )
        {
            this.monitor = monitor;
        }


        @Override
        public void run()
        {
            LOG.debug( "Metrics updater started." );
            try
            {
                for ( ResourceHost resourceHost : peerManager.getLocalPeer().getResourceHosts() )
                {

                    ResourceHostMetric resourceHostMetric =
                            new ResourceHostMetric( peerManager.getLocalPeer().getId() );
                    try
                    {
                        HostInfo hostInfo = hostRegistry.getHostInfoById( resourceHost.getId() );
                        resourceHostMetric.setHostInfo( new ResourceHostInfoModel( hostInfo ) );
                        ResourceHostMetric m = monitor.fetchResourceHostMetric( resourceHost );
                        if ( m != null )
                        {
                            resourceHostMetric.updateMetrics( m );
                        }
                        resourceHostMetric.setConnected( true );
                    }
                    catch ( HostDisconnectedException hde )
                    {
                        HostInfoModel defaultHostInfo =
                                new HostInfoModel( resourceHost.getId(), resourceHost.getHostname(),
                                        new HostInterfaces(), HostArchitecture.UNKNOWN );
                        resourceHostMetric.setHostInfo( defaultHostInfo );
                        resourceHostMetric.setConnected( false );
                    }

                    updateHostMetric( resourceHost.getId(), resourceHostMetric );

                    for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
                    {
                        BaseMetric containerHostMetric;
                        try
                        {
                            HostInfo hostInfo = hostRegistry.getHostInfoById( containerHost.getId() );
                            containerHostMetric =
                                    new BaseMetric( peerManager.getLocalPeer().getId(), new HostInfoModel( hostInfo ) );
                            containerHostMetric.setConnected( true );
                        }
                        catch ( HostDisconnectedException hde )
                        {
                            HostInfoModel disconnectedHostInfo =
                                    new HostInfoModel( containerHost.getId(), containerHost.getHostname(),
                                            new HostInterfaces(), HostArchitecture.UNKNOWN );
                            containerHostMetric =
                                    new BaseMetric( peerManager.getLocalPeer().getId(), disconnectedHostInfo );
                            containerHostMetric.setConnected( false );
                        }

                        updateHostMetric( containerHost.getId(), containerHostMetric );
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( e.getMessage(), e );
            }
            LOG.debug( "Metrics updater finished." );
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<ResourceAlert> alerts )
    {
        //        Host host;
        //        try
        //        {
        //            if ( "management".equals( resourceHostInfo.getHostname() ) )
        //            {
        //                host = peerManager.getLocalPeer().getManagementHost();
        //            }
        //            else
        //            {
        //                host = peerManager.getLocalPeer().getResourceHostByName( resourceHostInfo.getHostname() );
        //            }
        //        }
        //        catch ( HostNotFoundException e )
        //        {
        //            final String description =
        //                    String.format( "Resource host '%s' hot found. Id: %s", resourceHostInfo.getHostname(),
        //                            resourceHostInfo.getId() );
        //            Alert alert = new Alert(
        //                    new StringAlertValue( new HostId( resourceHostInfo.getId() ), HostType.RESOURCE_HOST,
        //                            AlertType.HOST_NOT_REGISTERED_ALERT, description ) );
        //            alertProcessor.process( alert );
        //            //TODO: sign RH key with peer key including management host
        //            return;
        //        }

        //        for ( ContainerHostInfo containerHostInfo : resourceHostInfo.getContainers() )
        //        {
        //            ContainerHost containerHost =
        //                    peerManager.getLocalPeer().findContainerById( new ContainerId( containerHostInfo.getId
        // () ) );
        //            if ( containerHost == null )
        //            {
        //                final String description =
        //                        String.format( "Container host '%s' hot found. Id: %s", containerHostInfo
        // .getHostname(),
        //                                containerHostInfo.getId() );
        //                Alert alert = new Alert(
        //                        new StringAlertValue( new HostId( containerHostInfo.getId() ), HostType
        // .CONTAINER_HOST,
        //                                AlertType.HOST_NOT_REGISTERED_ALERT, description ) );
        //
        //                alertProcessor.process( alert );
        //            }
        //        }
        if ( alerts != null )
        {
            for ( ResourceAlert resourceAlert : alerts )
            {
                queueAlertResource( new QuotaAlertResource( resourceAlert, System.currentTimeMillis() ) );
            }
        }
    }


    private void updateHostMetric( String id, final BaseMetric baseMetric )
    {
        this.metrics.put( id, baseMetric );
    }


    //    private class AlertProcessor
    //    {
    //        Map<String, AlertCounter> counters = new HashMap<>();
    //
    //
    //        void process( Alert alert )
    //        {
    //            AlertCounter counter = counters.get( alert.getId() );
    //            if ( counter == null )
    //            {
    //                counter = new AlertCounter();
    //                counters.put( alert.getId(), counter );
    //            }
    //            counter.inc();
    //            int c = counter.getValue();
    //            LOG.debug( String.format( "Alert value: %s %d", alert.getId(), c ) );
    //
    //            if ( c % 10 == 0 )
    //            {
    //                alert( alert );
    //                counter.reset();
    //            }
    //        }
    //    }
    //
    //
    //    private class AlertCounter
    //    {
    //        int value = 0;
    //
    //
    //        void inc()
    //        {
    //            value += 1;
    //        }
    //
    //
    //        public int getValue()
    //        {
    //            return value;
    //        }
    //
    //
    //        public void reset()
    //        {
    //            value = 0;
    //        }
    //    }
}
