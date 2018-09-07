package io.subutai.core.bazaarmanager.impl;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.security.RolesAllowed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.objects.TokenType;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.desktop.api.DesktopManager;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.api.BazaarRequester;
import io.subutai.core.bazaarmanager.api.RestClient;
import io.subutai.core.bazaarmanager.api.RestResult;
import io.subutai.core.bazaarmanager.api.StateLinkProcessor;
import io.subutai.core.bazaarmanager.api.dao.ConfigDataService;
import io.subutai.core.bazaarmanager.api.dao.ContainerMetricsService;
import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.api.model.Config;
import io.subutai.core.bazaarmanager.impl.appscale.AppScaleManager;
import io.subutai.core.bazaarmanager.impl.appscale.AppScaleProcessor;
import io.subutai.core.bazaarmanager.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.bazaarmanager.impl.dao.ContainerMetricsServiceImpl;
import io.subutai.core.bazaarmanager.impl.environment.state.Context;
import io.subutai.core.bazaarmanager.impl.http.BazaarRestClient;
import io.subutai.core.bazaarmanager.impl.model.ConfigEntity;
import io.subutai.core.bazaarmanager.impl.processor.HeartbeatProcessor;
import io.subutai.core.bazaarmanager.impl.processor.BazaarEnvironmentProcessor;
import io.subutai.core.bazaarmanager.impl.processor.ProxyProcessor;
import io.subutai.core.bazaarmanager.impl.processor.UserTokenProcessor;
import io.subutai.core.bazaarmanager.impl.processor.port_map.ContainerPortMapProcessor;
import io.subutai.core.bazaarmanager.impl.requestor.ContainerEventProcessor;
import io.subutai.core.bazaarmanager.impl.requestor.ContainerMetricsProcessor;
import io.subutai.core.bazaarmanager.impl.requestor.LogProcessor;
import io.subutai.core.bazaarmanager.impl.requestor.P2pStatusSender;
import io.subutai.core.bazaarmanager.impl.requestor.PeerMetricsProcessor;
import io.subutai.core.bazaarmanager.impl.requestor.VersionInfoProcessor;
import io.subutai.core.bazaarmanager.impl.tunnel.TunnelEventProcessor;
import io.subutai.core.bazaarmanager.impl.tunnel.TunnelProcessor;
import io.subutai.core.bazaarmanager.impl.util.EnvironmentUserHelper;
import io.subutai.core.bazaarmanager.impl.util.ReschedulableTimer;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.systemmanager.api.SystemManager;
import io.subutai.bazaar.share.common.BazaarEventListener;
import io.subutai.bazaar.share.dto.PeerDto;
import io.subutai.bazaar.share.dto.UserDto;


public class BazaarManagerImpl extends HostListener implements BazaarManager
{
    private static final long PEER_METRICS_SEND_INTERVAL_MIN = 10;
    private static final int CONTAINER_METRIC_SEND_INTERVAL_MIN = 15;

    private final Logger log = LoggerFactory.getLogger( getClass() );


    private final ScheduledExecutorService requestorsRunner = Executors.newScheduledThreadPool( 10 );

    private final ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService asyncHeartbeatExecutor = Executors.newFixedThreadPool( 3 );

    private SecurityManager securityManager;

    private EnvironmentManager envManager;

    private PeerManager peerManager;

    private ConfigManager configManager;

    private CommandExecutor commandExecutor;

    private DaoManager daoManager;

    private ConfigDataService configDataService;

    private Monitor monitor;

    private IdentityManager identityManager;

    private HeartbeatProcessor heartbeatProcessor;

    private P2pStatusSender p2pStatusSender;

    private ContainerEventProcessor containerEventProcessor;

    private final Set<BazaarEventListener> bazaarEventListeners = Sets.newConcurrentHashSet();

    private RestClient restClient;

    private LocalPeer localPeer;

    private EnvironmentUserHelper envUserHelper;

    private LogListenerImpl logListener;

    private PeerMetricsProcessor peerMetricsProcessor;

    private ContainerMetricsService containerMetricsService;

    private DesktopManager desktopManager;

    private SystemManager systemManager;

    private ContainerMetricsProcessor containerMetricsProcessor;

    private ReschedulableTimer peerMetricsTimer;


    public BazaarManagerImpl( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    public void init()
    {
        try
        {
            localPeer = peerManager.getLocalPeer();

            containerMetricsService = new ContainerMetricsServiceImpl( daoManager );

            configDataService = new ConfigDataServiceImpl( daoManager );

            configManager = new ConfigManager( securityManager, peerManager, identityManager );

            restClient = new BazaarRestClient( configManager );

            envUserHelper = new EnvironmentUserHelper( identityManager, configDataService, envManager, restClient );

            initBazaarRequesters();
            initHeartbeatProcessors();

            peerMetricsTimer = new ReschedulableTimer( new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        sendPeersMertics();
                    }
                    catch ( BazaarManagerException e )
                    {
                        log.error( "Error sending peer metrics: {}", e.getMessage() );
                    }
                }
            } );
        }
        catch ( Exception e )
        {
            log.error( e.getMessage() );
        }
    }


    @Override
    public void onHeartbeat( final ResourceHostInfo resourceHostInfo, final Set<QuotaAlertValue> alerts )
    {
    }


    private void initBazaarRequesters()
    {
        p2pStatusSender = new P2pStatusSender( this, localPeer, monitor, restClient );

        requestorsRunner.scheduleWithFixedDelay( p2pStatusSender, 30, 600, TimeUnit.SECONDS );

        //***********

        peerMetricsProcessor = new PeerMetricsProcessor( this, peerManager, configManager, monitor, restClient,
                PEER_METRICS_SEND_INTERVAL_MIN );

        requestorsRunner
                .scheduleWithFixedDelay( peerMetricsProcessor, 1, PEER_METRICS_SEND_INTERVAL_MIN, TimeUnit.MINUTES );

        //***********

        containerEventProcessor = new ContainerEventProcessor( this, peerManager, restClient, desktopManager );

        requestorsRunner.scheduleWithFixedDelay( containerEventProcessor, 30, 300, TimeUnit.SECONDS );

        //***********

        LogProcessor logProcessor = new LogProcessor( configManager, this, logListener, restClient );

        requestorsRunner.scheduleWithFixedDelay( logProcessor, 40, 3600, TimeUnit.SECONDS );

        //***********

        TunnelEventProcessor tunnelEventProcessor =
                new TunnelEventProcessor( this, peerManager, configManager, restClient );

        requestorsRunner.scheduleWithFixedDelay( tunnelEventProcessor, 20, 300, TimeUnit.SECONDS );

        //***********

        final VersionInfoProcessor versionInfoProcessor =
                new VersionInfoProcessor( this, peerManager, configManager, restClient );

        requestorsRunner.scheduleWithFixedDelay( versionInfoProcessor, 20, 60, TimeUnit.SECONDS );


        //***********

        EnvironmentTelemetryProcessor environmentTelemetryProcessor =
                new EnvironmentTelemetryProcessor( this, peerManager, configManager, restClient );

        requestorsRunner.scheduleWithFixedDelay( environmentTelemetryProcessor, 20, 1800, TimeUnit.SECONDS );

        //***********
        containerMetricsProcessor =
                new ContainerMetricsProcessor( this, localPeer, monitor, restClient, containerMetricsService,
                        CONTAINER_METRIC_SEND_INTERVAL_MIN );
        requestorsRunner.scheduleWithFixedDelay( containerMetricsProcessor, 1, CONTAINER_METRIC_SEND_INTERVAL_MIN,
                TimeUnit.MINUTES );
    }


    private void initHeartbeatProcessors()
    {
        StateLinkProcessor tunnelProcessor = new TunnelProcessor( peerManager, restClient );

        Context ctx =
                new Context( this, identityManager, envManager, envUserHelper, localPeer, restClient, desktopManager );

        StateLinkProcessor bazaarEnvironmentProcessor = new BazaarEnvironmentProcessor( ctx );

        AppScaleProcessor appScaleProcessor = new AppScaleProcessor( new AppScaleManager( peerManager ), restClient );

        EnvironmentTelemetryProcessor environmentTelemetryProcessor =
                new EnvironmentTelemetryProcessor( this, peerManager, configManager, restClient );

        ContainerPortMapProcessor containerPortMapProcessor = new ContainerPortMapProcessor( ctx );

        ProxyProcessor proxyProcessor = new ProxyProcessor( peerManager, restClient );

        UserTokenProcessor userTokenProcessor = new UserTokenProcessor( ctx );

        heartbeatProcessor = new HeartbeatProcessor( this, peerManager, restClient, localPeer.getId() )
                .addProcessor( bazaarEnvironmentProcessor ).addProcessor( tunnelProcessor )
                .addProcessor( environmentTelemetryProcessor ).addProcessor( appScaleProcessor )
                .addProcessor( proxyProcessor ).addProcessor( containerPortMapProcessor )
                .addProcessor( userTokenProcessor );

        heartbeatExecutorService
                .scheduleWithFixedDelay( heartbeatProcessor, 5, HeartbeatProcessor.SMALL_INTERVAL_SECONDS,
                        TimeUnit.SECONDS );
    }


    @Override
    public void sendHeartbeat() throws BazaarManagerException
    {
        if ( isRegisteredWithBazaar() )
        {
            p2pStatusSender.request();
            heartbeatProcessor.sendHeartbeat( true );
            containerEventProcessor.request();
        }
    }


    @Override
    public void sendPeersMertics() throws BazaarManagerException
    {
        if ( isRegisteredWithBazaar() )
        {
            peerMetricsProcessor.request();
        }
    }


    @Override
    public void sendContainerMertics() throws BazaarManagerException
    {
        if ( isRegisteredWithBazaar() )
        {
            containerMetricsProcessor.request();
        }
    }


    /**
     * Called by Bazaar to trigger heartbeat on peer
     */
    @Override
    public void triggerHeartbeat()
    {
        asyncHeartbeatExecutor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    heartbeatProcessor.sendHeartbeat( true );
                }
                catch ( Exception e )
                {
                    log.error( "Error on triggering heartbeat: ", e );
                }
            }
        } );
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void registerPeer( String email, String password, String peerName, String peerScope )
            throws BazaarManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( email ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( password ) );

        RegistrationManager registrationManager = new RegistrationManager( this, configManager );

        registrationManager.registerPeer( email, password, peerName, peerScope );

        notifyRegistrationListeners();

        peerMetricsProcessor.request();
    }


    public void setPeerName( String peerName )
    {
        Preconditions.checkArgument( !StringUtils.isBlank( peerName ), "Invalid peer name" );

        ConfigEntity configEntity = ( ConfigEntity ) getConfigDataService().getBazaarConfig( localPeer.getId() );
        configEntity.setPeerName( peerName );
        getConfigDataService().saveBazaarConfig( configEntity );
    }


    private void notifyRegistrationListeners()
    {
        if ( !CollectionUtil.isCollectionEmpty( bazaarEventListeners ) )
        {
            ExecutorService notifier = Executors.newFixedThreadPool( bazaarEventListeners.size() );

            for ( final BazaarEventListener bazaarEventListener : bazaarEventListeners )
            {
                notifier.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            bazaarEventListener.onRegistrationSucceeded();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error notifying event listener", e );
                        }
                    }
                } );
            }

            notifier.shutdown();
        }
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void unregisterPeer() throws BazaarManagerException
    {
        RegistrationManager registrationManager = new RegistrationManager( this, configManager );

        registrationManager.unregister();

        if ( !CollectionUtil.isCollectionEmpty( bazaarEventListeners ) )
        {
            ExecutorService notifier = Executors.newFixedThreadPool( bazaarEventListeners.size() );

            for ( final BazaarEventListener bazaarEventListener : bazaarEventListeners )
            {
                notifier.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            bazaarEventListener.onUnregister();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error notifying event listener", e );
                        }
                    }
                } );
            }

            notifier.shutdown();
        }
    }


    @Override
    public String getBazaarIp() throws BazaarManagerException
    {
        Config config = getConfigDataService().getBazaarConfig( configManager.getPeerId() );

        if ( config != null )
        {
            return config.getBazaarIp();
        }
        else
        {
            return null;
        }
    }


    @Override
    public boolean isRegisteredWithBazaar()
    {
        return getBazaarConfiguration() != null;
    }


    @Override
    public boolean isBazaarReachable()
    {
        return heartbeatProcessor != null && heartbeatProcessor.isBazaarReachable();
    }


    @Override
    public boolean canWorkWithBazaar()
    {
        return isBazaarReachable() && isRegisteredWithBazaar();
    }


    public boolean isPeerUpdating()
    {
        return systemManager.isUpdateInProgress();
    }


    @Override
    public boolean hasBazaarTasksInAction()
    {
        return BazaarRequester.areRequestorsRunning() || HeartbeatProcessor.areProcessorsRunning();
    }


    @Override
    public String getPeerName()
    {
        Config config = getBazaarConfiguration();

        if ( config != null )
        {
            return config.getPeerName();
        }

        return null;
    }


    @Override
    public Map<String, String> getPeerInfo() throws BazaarManagerException
    {
        Map<String, String> result = new HashMap<>();

        try
        {
            String path = "/rest/v1/peers/" + configManager.getPeerId();

            RestResult<PeerDto> restResult = restClient.get( path, PeerDto.class );

            if ( restResult.getStatus() == HttpStatus.SC_OK )
            {
                PeerDto dto = restResult.getEntity();

                result.put( "OwnerId", dto != null ? dto.getOwnerId() : null );

                log.debug( "PeerDto: " + result.toString() );
            }
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( "Could not retrieve Peer info", e );
        }

        return result;
    }


    @Override
    public void notifyBazaarThatPeerIsOffline()
    {
        heartbeatProcessor.notifyBazaarThatPeerIsOffline();
    }


    @Override
    public Config getBazaarConfiguration()
    {
        return configDataService.getBazaarConfig( configManager.getPeerId() );
    }


    public RestClient getRestClient()
    {
        return restClient;
    }


    @Override
    public String getCurrentUserEmail()
    {
        User currentUser = identityManager.getActiveUser();

        String email = currentUser.getEmail();

        log.info( "currentUser: id={}, username={}, email={}", currentUser.getId(), currentUser.getUserName(), email );

        if ( !email.contains( BAZAAR_EMAIL_SUFFIX ) && isRegisteredWithBazaar() )
        {
            return getBazaarConfiguration().getOwnerEmail();
        }

        UserDto userDto = envUserHelper.getUserDataFromBazaar( StringUtils.substringBefore( email, "@" ) );

        return userDto.getEmail();
    }


    public void addListener( BazaarEventListener listener )
    {
        if ( listener != null )
        {
            bazaarEventListeners.add( listener );
        }
    }


    public void removeListener( BazaarEventListener listener )
    {
        if ( listener != null )
        {
            bazaarEventListeners.remove( listener );
        }
    }


    public void destroy()
    {
        heartbeatExecutorService.shutdown();

        requestorsRunner.shutdown();

        asyncHeartbeatExecutor.shutdown();
    }


    public CommandExecutor getCommandExecutor()
    {
        return commandExecutor;
    }


    public void setCommandExecutor( final CommandExecutor commandExecutor )
    {
        this.commandExecutor = commandExecutor;
    }


    public void setEnvironmentManager( final EnvironmentManager environmentManager )
    {
        this.envManager = environmentManager;
    }


    public void setLogListener( final LogListenerImpl logListener )
    {
        this.logListener = logListener;
    }


    public void setSecurityManager( final SecurityManager securityManager )
    {
        this.securityManager = securityManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public ConfigDataService getConfigDataService()
    {
        return configDataService;
    }


    public void setConfigDataService( final ConfigDataService configDataService )
    {
        this.configDataService = configDataService;
    }


    public void setDesktopManager( final DesktopManager desktopManager )
    {
        this.desktopManager = desktopManager;
    }


    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setSystemManager( final SystemManager systemManager )
    {
        this.systemManager = systemManager;
    }


    @Override
    public void onRhConnected( final ResourceHostInfo newRhInfo )
    {
        try
        {
            if ( isRegisteredWithBazaar() )
            {
                //delay to let peer register the RH
                TaskUtil.sleep( 1000 );

                log.debug( "Notifying Bazaar about RH connection" );

                peerMetricsProcessor.request();
            }
        }
        catch ( Exception e )
        {
            log.error( "Error sending peer metrics", e );
        }
    }


    @Override
    public void onRhDisconnected( final ResourceHostInfo resourceHostInfo )
    {
        try
        {
            if ( isRegisteredWithBazaar() )
            {
                //delay to let peer unregister the RH
                TaskUtil.sleep( 1000 );

                log.debug( "Notifying Bazaar about RH disconnection" );

                peerMetricsProcessor.request();
            }
        }
        catch ( Exception e )
        {
            log.error( "Error sending peer metrics", e );
        }
    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {
        schedulePeerMetrics();
    }


    /**
     * This method schedules sending of peer metrics to Bazaar. If a pending round is still there it gets rescheduled.
     * This is done to not overwhelm Bazaar with frequent requests that can happen for example when environment is
     * destroyed and its containers get destroyed one by one very quickly.
     */
    @Override
    public void schedulePeerMetrics()
    {
        if ( isRegisteredWithBazaar() )
        {
            peerMetricsTimer.schedule( 15L );
        }
    }


    @Override
    synchronized public UserToken getUserToken( final String envOwnerId, final String peerId )
    {
        final User user = envUserHelper.handleEnvironmentOwnerCreation( envOwnerId, peerId );
        UserToken token = identityManager.getUserToken( user.getId() );
        if ( token == null )
        {
            token = identityManager.createUserToken( user, null, null, null, TokenType.SESSION.getId(), null );
        }
        return token;
    }
}