package io.subutai.core.metric.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
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
import io.subutai.common.metric.AlertValue;
import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.HistoricalMetric;
import io.subutai.common.metric.MetricType;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.metric.ResourceAlertValue;
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
    private static final String ENVIRONMENT_IS_NULL_MSG = "Environment is null";
    private static final String CONTAINER_IS_NULL_MSG = "Container is null";
    private static final String INVALID_SUBSCRIBER_ID_MSG = "Invalid subscriber id";
    private static final String SETTINGS_IS_NULL_MSG = "Settings is null";
    private static final int METRICS_UPDATE_DELAY = 60;

    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class );
    private final HostRegistry hostRegistry;

    //    protected Set<AlertListener> alertListeners =
    //            Collections.newSetFromMap( new ConcurrentHashMap<AlertListener, Boolean>() );
    protected Map<String, AlertListener> alertListeners = new ConcurrentHashMap<>();

    private final Commands commands = new Commands();
    private final EnvironmentManager environmentManager;
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MonitorDao monitorDao;
    protected DaoManager daoManager;
    private Cache<String, BaseMetric> metrics = CacheBuilder.newBuilder().
            expireAfterWrite( METRICS_UPDATE_DELAY * 2, TimeUnit.SECONDS ).
                                                                    build();
    protected ScheduledExecutorService stateUpdateExecutorService;
    protected ScheduledExecutorService alertDeliverExecutorService;

    private Map<String, AlertPack> localAlerts = new ConcurrentHashMap<>();
    private Set<AlertPack> alerts = new CopyOnWriteArraySet<>();

    private PeerManager peerManager;
    //    private AlertProcessor alertProcessor = new AlertProcessor();


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
        alertDeliverExecutorService = Executors.newScheduledThreadPool( 1 );
        alertDeliverExecutorService.scheduleWithFixedDelay( new AlertDeliver(), 10, 30, TimeUnit.SECONDS );
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


    protected void deliverAlertPackets()
    {
        for ( AlertPack alertPack : localAlerts.values() )
        {
            if ( !alertPack.isDelivered() )
            {
                try
                {
                    Peer peer = peerManager.getPeer( alertPack.getPeerId() );
                    peer.alert( alertPack );
                    alertPack.setDelivered( true );
                    LOG.debug( "Alert package delivered: " + alertPack.getValue().getId() );
                }
                catch ( Exception e )
                {
                    LOG.warn( "Error on delivering alert package: " + alertPack.getValue().getId() );
                }
            }
        }
    }


    public void destroy()
    {
        notificationExecutor.shutdown();
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


    @Override
    public void addAlert( final AlertPack alert )
    {
        AlertPack a = new AlertPack( alert.getPeerId(), alert.getEnvironmentId(), alert.getContainerId(),
                alert.getTemplateName(), alert.getValue() );
        alerts.add( a );
    }


    protected void putAlert( AlertValue alert )
    {

        //        if ( !isValidAlert( alert ) )
        //        {
        //            return;
        //        }

        if ( localAlerts.get( alert.getId() ) != null )
        {
            // skipping, alert already exists
            LOG.debug( String.format( "Alert already exists: ", alert.getId() ) );
        }

        String containerId = alert.getHostId().getId();

        try
        {
            ContainerHost host = peerManager.getLocalPeer().getContainerHostById( containerId );
            AlertPack packet = new AlertPack( host.getInitiatorPeerId(), host.getEnvironmentId().getId(), containerId,
                    host.getTemplateName(), alert );
            LOG.debug( String.format( "Put alert: %s", alert.getId() ) );
            localAlerts.put( alert.getId(), packet );
        }
        catch ( HostNotFoundException e )
        {
            LOG.warn( e.getMessage() );
        }
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
        return Collections.unmodifiableCollection( localAlerts.values() );
    }


    protected void notifyListener( final AlertPack alertPack )
    {
        AlertListener alertListener = alertListeners.get( alertPack.getTemplateName() );

        if ( alertListener != null )
        {
            AlertNotifier alertNotifier = new AlertNotifier( alertPack, alertListener );
            alertNotifier.run();
        }
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
    public Set<AlertPack> getAlertPackages()
    {
        return alerts;
    }


    @Override
    public void notifyAlertListeners()
    {
        for ( AlertPack alertPack : alerts )
        {
            if ( !alertPack.isDelivered() )
            {
                notifyListener( alertPack );
                alertPack.setDelivered( true );
            }
        }
    }


    private class AlertDeliver implements Runnable
    {
        @Override
        public void run()
        {
            deliverAlertPackets();
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
                        resourceHostMetric.updateMetrics( m );
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
                //                final Alert alert = new Alert( new ResourceAlertValue( resourceAlert ) );

                putAlert( new ResourceAlertValue( resourceAlert ) );

                //                alertProcessor.process( alert );
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
