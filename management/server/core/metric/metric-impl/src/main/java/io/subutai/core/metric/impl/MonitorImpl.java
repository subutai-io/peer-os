package io.subutai.core.metric.impl;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang.time.DateUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.exception.DaoException;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInfo;
import io.subutai.common.host.HostInfoModel;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.host.ResourceHostInfoModel;
import io.subutai.common.metric.Alert;
import io.subutai.common.metric.BaseMetric;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.QuotaAlert;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.HostNotFoundException;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.resource.HistoricalMetrics;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hostregistry.api.HostDisconnectedException;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.api.MonitoringSettings;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor, HostListener
{
    private static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class );

    private static final String ENVIRONMENT_IS_NULL_MSG = "Environment is null";
    private static final String CONTAINER_IS_NULL_MSG = "Container is null";
    private static final String INVALID_SUBSCRIBER_ID_MSG = "Invalid subscriber id";
    private static final String SETTINGS_IS_NULL_MSG = "Settings is null";

    private static final int METRICS_UPDATE_DELAY = 60;
    private static final int ALERT_LIVE_TIME = 2;// alert live time in min
    private final HostRegistry hostRegistry;

    //    protected Set<AlertHandler> alertHandlers =
    //            Collections.newSetFromMap( new ConcurrentHashMap<AlertHandler, Boolean>() );
    protected Map<String, AlertHandler> alertHandlers = new ConcurrentHashMap<String, AlertHandler>();

    private final Commands commands = new Commands();
    private final EnvironmentManager environmentManager;
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
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
        //        Set<ContainerHost> a = new HashSet<>();
        //        a.addAll( environment.getContainerHosts() );
        //        activateMonitoring( a, monitoringSettings, environment.getId() );
    }


    //    @Override
    //    public void startMonitoring( final String subscriberId, final ContainerHost containerHost,
    //                                 final MonitoringSettings monitoringSettings ) throws MonitorException
    //    {
    //        Preconditions.checkArgument( !Strings.isNullOrEmpty( subscriberId ), INVALID_SUBSCRIBER_ID_MSG );
    //        Preconditions.checkNotNull( containerHost, CONTAINER_IS_NULL_MSG );
    //        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );
    //
    //        //make sure subscriber id is truncated to 100 characters
    //        String trimmedSubscriberId = StringUtil.trimToSize( subscriberId, Constants.MAX_SUBSCRIBER_ID_LEN );
    //
    //
    //        //save subscription to database
    //        try
    //        {
    //            String environmentId =
    //                    containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId() : null;
    //            monitorDao.addSubscription( environmentId, trimmedSubscriberId );
    //        }
    //        catch ( DaoException e )
    //        {
    //            LOG.error( "Error in startMonitoring", e );
    //            throw new MonitorException( e );
    //        }
    //
    ////        //activate monitoring
    ////        String environmentId =
    ////                containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId() : null;
    ////        activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, environmentId );
    //    }


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


    //    @Override
    //    public void activateMonitoring( final ContainerHost containerHost, final MonitoringSettings
    // monitoringSettings/*,
    //         final String environmentId*/ ) throws MonitorException
    //
    //    {
    //        Preconditions.checkNotNull( containerHost, CONTAINER_IS_NULL_MSG );
    //        Preconditions.checkNotNull( monitoringSettings, SETTINGS_IS_NULL_MSG );
    //
    //        String environmentId =
    //                containerHost instanceof EnvironmentContainerHost ? containerHost.getEnvironmentId().getId() :
    // null;
    //        activateMonitoring( Sets.newHashSet( containerHost ), monitoringSettings, environmentId );
    //    }
    //
    //
    //    protected void activateMonitoring( Set<ContainerHost> containerHosts, MonitoringSettings monitoringSettings,
    //                                       String environmentId ) throws MonitorException
    //    {
    //        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
    //        Map<Peer, Set<ContainerHost>> peersContainers = Maps.newHashMap();
    //
    //        for ( ContainerHost containerHost : containerHosts )
    //        {
    //            try
    //            {
    //                Peer peer = containerHost.getPeer();
    //
    //                Set<ContainerHost> containers = peersContainers.get( peer );
    //
    //                if ( containers == null )
    //                {
    //                    containers = Sets.newHashSet();
    //                    peersContainers.put( peer, containers );
    //                }
    //
    //                containers.add( containerHost );
    //            }
    //            catch ( Exception e )
    //            {
    //                LOG.error( String.format( "Could not obtain peer for container %s", containerHost.getHostname()
    // ), e );
    //                throw new MonitorException( e );
    //            }
    //        }
    //
    //
    //        for ( Map.Entry<Peer, Set<ContainerHost>> peerContainers : peersContainers.entrySet() )
    //        {
    //            Peer peer = peerContainers.getKey();
    //            Set<ContainerHost> containers = peerContainers.getValue();
    //
    //            if ( peer.isLocal() )
    //            {
    //                activateMonitoringAtLocalContainers( containers, monitoringSettings );
    //            }
    //            else
    //            {
    //                activateMonitoringAtRemoteContainers( peer, containers, monitoringSettings, environmentId );
    //            }
    //        }
    //    }
    //
    //
    //    protected void activateMonitoringAtRemoteContainers( Peer peer, Set<ContainerHost> containerHosts,
    //                                                         MonitoringSettings monitoringSettings, String
    // environmentId )
    //    {
    //        Preconditions.checkNotNull( peer );
    //        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
    //
    //        try
    //        {
    //            //*********construct Secure Header ****************************
    //            Map<String, String> headers = Maps.newHashMap();
    //            //*************************************************************
    //
    //            peer.sendRequest( new MonitoringActivationRequest( containerHosts, monitoringSettings ),
    //                    RecipientType.MONITORING_ACTIVATION_RECIPIENT.name(), Constants.MONITORING_ACTIVATION_TIMEOUT,
    //                    headers );
    //        }
    //        catch ( PeerException e )
    //        {
    //            LOG.error( "Error in activateMonitoringAtRemoteContainers", e );
    //        }
    //    }
    //
    //
    //    protected void activateMonitoringAtLocalContainers( Set<ContainerHost> containerHosts,
    //                                                        MonitoringSettings monitoringSettings )
    //    {
    //        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHosts ) );
    //        Preconditions.checkNotNull( monitoringSettings );
    //
    //        for ( ContainerHost containerHost : containerHosts )
    //        {
    //            try
    //            {
    //                ResourceHost resourceHost =
    //                        peerManager.getLocalPeer().getResourceHostByContainerId( containerHost.getId() );
    //                CommandResult commandResult = resourceHost.execute(
    //                        commands.getActivateMonitoringCommand( containerHost.getHostname(), monitoringSettings
    // ) );
    //                if ( !commandResult.hasSucceeded() )
    //                {
    //                    LOG.error( String.format( "Error activating metrics on %s: %s %s", containerHost
    // .getHostname(),
    //                            commandResult.getStatus(), commandResult.getStdErr() ) );
    //                }
    //            }
    //            catch ( Exception e )
    //            {
    //                LOG.error( "Error in activateMonitoringAtLocalContainers", e );
    //            }
    //        }
    //    }


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

            CommandResult commandResult =
                    peerManager.getLocalPeer().getManagementHost().execute( historicalMetricCommand );

            if ( commandResult.hasSucceeded() )
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


    protected void queueAlertResource( Alert alert )
    {
        if ( alert == null || !alert.validate() )
        {
            // skipping invalid alert
            return;
        }

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


    private AlertPack buildAlertPack( Alert alert )
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
        //          Example of usage:
        //                QuotaAlertValue quotaAlertValue = alert.getAlertValue( QuotaAlertValue.class );
        //                StringAlertValue stringAlertValue = alert.getAlertValue( StringAlertValue.class );
        //                ExceededQuota v1 = quotaAlertValue.getValue();
        //                String v2 = stringAlertValue.getValue();

        return packet;
    }


    protected Date buildExpireTime()
    {
        return DateUtils.addMinutes( new Date(), ALERT_LIVE_TIME );
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
    public void addAlertHandler( final AlertHandler alertHandler )
    {
        if ( alertHandler != null && alertHandler.getHandlerId() != null
                && alertHandler.getAlertHandlerPriority() != null )
        {
            this.alertHandlers.put( alertHandler.getHandlerId(), alertHandler );
        }
        else
        {
            LOG.warn( "Alert handler rejected: " + alertHandler );
        }
    }


    @Override
    public void removeAlertHandler( final AlertHandler alertHandler )
    {
        if ( alertHandler != null )
        {
            this.alertHandlers.remove( alertHandler.getHandlerId() );
        }
    }


    @Override
    public Collection<AlertHandler> getAlertHandlers()
    {
        List<AlertHandler> result = new ArrayList<>( alertHandlers.values() );
        Collections.sort( result, new AlertHandlerComparator() );
        return result;
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
            if ( !alertPack.isDelivered() && !alertPack.isExpired() )
            {
                LOG.debug( "Notifying: " + alertPack );
                notifyOnAlert( alertPack );
                alertPack.setDelivered( true );
            }
        }
    }


    protected void notifyOnAlert( final AlertPack alertPack )
    {
        try
        {
            Environment environment = environmentManager.loadEnvironment( alertPack.getEnvironmentId() );

            Set<String> handlerList = monitorDao.getEnvironmentSubscribersIds( alertPack.getEnvironmentId() );

            List<AlertHandler> handlers = new ArrayList<>();

            for ( String handlerId : handlerList )
            {
                AlertHandler alertHandler = alertHandlers.get( handlerId );
                if ( alertHandler == null )
                {
                    alertPack.addLog(
                            String.format( "Environment '%s' subscribed to handler '%s', but handler not found.",
                                    alertPack.getEnvironmentId(), handlerId ) );
                }
            }

            Collections.sort( handlers, new AlertHandlerComparator() );

            handleAlertPack( alertPack, handlers );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in handling alert package.", e );
        }
    }


    protected void handleAlertPack( final AlertPack alertPack, List<AlertHandler> alertHandlers )
    {

        for ( final AlertHandler handler : alertHandlers )
        {
            try
            {
                alertPack.addLog( String.format( "Invoking pre-processor of '%s'.", handler.getHandlerId() ) );
                handler.preProcess( alertPack );
                alertPack.addLog( String.format( "Pre-processor of '%s' finished.", handler.getHandlerId() ) );
                alertPack.addLog( String.format( "Invoking main processor of '%s'.", handler.getHandlerId() ) );
                handler.process( alertPack );
                alertPack.addLog( String.format( "Main processor of '%s' finished.", handler.getHandlerId() ) );
                alertPack.addLog( String.format( "Invoking post-processor of '%s'.", handler.getHandlerId() ) );
                handler.postProcess( alertPack );
                alertPack.addLog( String.format( "Pre-processor of '%s' finished.", handler.getHandlerId() ) );
            }
            catch ( Exception e )
            {
                alertPack.addLog( e.getMessage() );
            }
        }
    }


    @Override
    public void deliverAlerts()
    {
        for ( AlertPack alertPack : localALerts.values() )
        {
            if ( !alertPack.isDelivered() )
            {
                deliverAlertPackToPeer( alertPack );
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


    private void deliverAlertPackToPeer( final AlertPack alertPack )
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
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
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
            for ( QuotaAlertValue quotaAlertValue : alerts )
            {
                queueAlertResource( new QuotaAlert( quotaAlertValue, System.currentTimeMillis() ) );
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
