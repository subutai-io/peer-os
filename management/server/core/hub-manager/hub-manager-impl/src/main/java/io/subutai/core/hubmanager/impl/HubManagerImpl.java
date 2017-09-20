package io.subutai.core.hubmanager.impl;


import java.io.File;
import java.io.InputStream;
import java.util.Date;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaceModel;
import io.subutai.common.host.ResourceHostInfo;
import io.subutai.common.metric.QuotaAlertValue;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.security.utils.SafeCloseUtil;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.TaskUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hostregistry.api.HostListener;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.RestClient;
import io.subutai.core.hubmanager.api.RestResult;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.api.dao.ConfigDataService;
import io.subutai.core.hubmanager.api.dao.ContainerMetricsService;
import io.subutai.core.hubmanager.api.exception.HubManagerException;
import io.subutai.core.hubmanager.api.model.Config;
import io.subutai.core.hubmanager.impl.appscale.AppScaleManager;
import io.subutai.core.hubmanager.impl.appscale.AppScaleProcessor;
import io.subutai.core.hubmanager.impl.dao.ConfigDataServiceImpl;
import io.subutai.core.hubmanager.impl.dao.ContainerMetricsServiceImpl;
import io.subutai.core.hubmanager.impl.environment.state.Context;
import io.subutai.core.hubmanager.impl.http.HubRestClient;
import io.subutai.core.hubmanager.impl.processor.HeartbeatProcessor;
import io.subutai.core.hubmanager.impl.processor.HubEnvironmentProcessor;
import io.subutai.core.hubmanager.impl.processor.ProductProcessor;
import io.subutai.core.hubmanager.impl.processor.ProxyProcessor;
import io.subutai.core.hubmanager.impl.processor.UserTokenProcessor;
import io.subutai.core.hubmanager.impl.processor.port_map.ContainerPortMapProcessor;
import io.subutai.core.hubmanager.impl.requestor.ContainerEventProcessor;
import io.subutai.core.hubmanager.impl.requestor.ContainerMetricsProcessor;
import io.subutai.core.hubmanager.impl.requestor.HubLoggerProcessor;
import io.subutai.core.hubmanager.impl.requestor.P2pLogsSender;
import io.subutai.core.hubmanager.impl.requestor.PeerMetricsProcessor;
import io.subutai.core.hubmanager.impl.requestor.VersionInfoProcessor;
import io.subutai.core.hubmanager.impl.tunnel.TunnelEventProcessor;
import io.subutai.core.hubmanager.impl.tunnel.TunnelProcessor;
import io.subutai.core.hubmanager.impl.util.EnvironmentUserHelper;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.hub.share.common.HubEventListener;
import io.subutai.hub.share.dto.PeerDto;
import io.subutai.hub.share.dto.PeerProductDataDto;
import io.subutai.hub.share.dto.SystemConfDto;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.product.ProductsDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubManagerImpl implements HubManager, HostListener
{
    private static final long P2P_LOG_SEND_INTERVAL_SEC = TimeUnit.MINUTES.toSeconds( 15 );
    private static final long METRICS_SEND_INTERVAL_SEC = TimeUnit.MINUTES.toSeconds( 10 );
    private static final int CONTAINER_METRIC_SEND_INTERVAL_MIN = 15;

    private final Logger log = LoggerFactory.getLogger( getClass() );

    private final ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService peerLogsExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService resourceHostMonitorExecutorService =
            Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService peerMetricsExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService hubLoggerExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService containerEventExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService environmentTelemetryService = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService versionEventExecutor = Executors.newSingleThreadScheduledExecutor();

    private final ScheduledExecutorService tunnelEventService = Executors.newSingleThreadScheduledExecutor();

    private final ExecutorService asyncHeartbeatExecutor = Executors.newFixedThreadPool( 3 );

    private final ScheduledExecutorService containersMetricsExecutorService =
            Executors.newSingleThreadScheduledExecutor();

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

    private P2pLogsSender p2pLogsSender;

    private ContainerEventProcessor containerEventProcessor;

    private final Set<HubEventListener> hubEventListeners = Sets.newConcurrentHashSet();

    private RestClient restClient;

    private LocalPeer localPeer;

    private EnvironmentUserHelper envUserHelper;

    private LogListenerImpl logListener;

    private PeerMetricsProcessor peerMetricsProcessor;

    private ContainerMetricsService containerMetricsService;

    private ProductProcessor productProcessor;


    public HubManagerImpl( DaoManager daoManager )
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

            restClient = new HubRestClient( configManager );

            envUserHelper = new EnvironmentUserHelper( identityManager, configDataService, envManager, restClient );

            initHubRequesters();
            initHeartbeatProcessors();
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


    private void initHubRequesters()
    {
        p2pLogsSender = new P2pLogsSender( this, localPeer, monitor, restClient );

        peerLogsExecutor.scheduleWithFixedDelay( p2pLogsSender, 20, P2P_LOG_SEND_INTERVAL_SEC, TimeUnit.SECONDS );

        //***********

        peerMetricsProcessor = new PeerMetricsProcessor( this, peerManager, configManager, monitor, restClient,
                ( int ) METRICS_SEND_INTERVAL_SEC );

        peerMetricsExecutorService
                .scheduleWithFixedDelay( peerMetricsProcessor, 30, METRICS_SEND_INTERVAL_SEC, TimeUnit.SECONDS );

        //***********

        containerEventProcessor = new ContainerEventProcessor( this, peerManager, restClient );

        containerEventExecutor.scheduleWithFixedDelay( containerEventProcessor, 30, 300, TimeUnit.SECONDS );

        //***********

        HubLoggerProcessor hubLoggerProcessor = new HubLoggerProcessor( configManager, this, logListener, restClient );

        hubLoggerExecutorService.scheduleWithFixedDelay( hubLoggerProcessor, 40, 3600, TimeUnit.SECONDS );

        //***********

        TunnelEventProcessor tunnelEventProcessor =
                new TunnelEventProcessor( this, peerManager, configManager, restClient );

        tunnelEventService.scheduleWithFixedDelay( tunnelEventProcessor, 20, 300, TimeUnit.SECONDS );

        //***********

        final VersionInfoProcessor versionInfoProcessor =
                new VersionInfoProcessor( this, peerManager, configManager, restClient );

        versionEventExecutor.scheduleWithFixedDelay( versionInfoProcessor, 20, 120, TimeUnit.SECONDS );


        //***********

        EnvironmentTelemetryProcessor environmentTelemetryProcessor =
                new EnvironmentTelemetryProcessor( this, peerManager, configManager, restClient );

        environmentTelemetryService.scheduleWithFixedDelay( environmentTelemetryProcessor, 20, 1800, TimeUnit.SECONDS );

        //***********
        final ContainerMetricsProcessor containersMetricsProcessor =
                new ContainerMetricsProcessor( this, localPeer, monitor, restClient, containerMetricsService,
                        CONTAINER_METRIC_SEND_INTERVAL_MIN );
        containersMetricsExecutorService
                .scheduleWithFixedDelay( containersMetricsProcessor, 1, CONTAINER_METRIC_SEND_INTERVAL_MIN,
                        TimeUnit.MINUTES );
    }


    private void initHeartbeatProcessors()
    {
        StateLinkProcessor tunnelProcessor = new TunnelProcessor( peerManager, restClient );

        Context ctx = new Context( identityManager, envManager, envUserHelper, localPeer, restClient );

        StateLinkProcessor hubEnvironmentProcessor = new HubEnvironmentProcessor( ctx );

        productProcessor = new ProductProcessor( configManager, this.hubEventListeners, restClient );

        AppScaleProcessor appScaleProcessor = new AppScaleProcessor( new AppScaleManager( peerManager ), restClient );

        EnvironmentTelemetryProcessor environmentTelemetryProcessor =
                new EnvironmentTelemetryProcessor( this, peerManager, configManager, restClient );

        ContainerPortMapProcessor containerPortMapProcessor = new ContainerPortMapProcessor( ctx );

        ProxyProcessor proxyProcessor = new ProxyProcessor( peerManager, restClient );

        UserTokenProcessor userTokenProcessor = new UserTokenProcessor( ctx );

        heartbeatProcessor =
                new HeartbeatProcessor( this, restClient, localPeer.getId() ).addProcessor( hubEnvironmentProcessor )
                                                                             .addProcessor( tunnelProcessor )
                                                                             .addProcessor(
                                                                                     environmentTelemetryProcessor )
                                                                             .addProcessor( productProcessor )
                                                                             .addProcessor( appScaleProcessor )
                                                                             .addProcessor( proxyProcessor )
                                                                             .addProcessor( containerPortMapProcessor )
                                                                             .addProcessor( userTokenProcessor );

        heartbeatExecutorService
                .scheduleWithFixedDelay( heartbeatProcessor, 5, HeartbeatProcessor.SMALL_INTERVAL_SECONDS,
                        TimeUnit.SECONDS );
    }


    @Override
    public void sendHeartbeat() throws HubManagerException
    {
        p2pLogsSender.process();
        heartbeatProcessor.sendHeartbeat( true );
        containerEventProcessor.process();
    }


    /**
     * Called by Hub to trigger heartbeat on peer
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
            throws HubManagerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( email ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( password ) );

        RegistrationManager registrationManager = new RegistrationManager( this, configManager );

        registrationManager.registerPeer( email, password, peerName, peerScope );

        notifyRegistrationListeners();

        peerMetricsProcessor.request();
    }


    private void notifyRegistrationListeners()
    {
        if ( !CollectionUtil.isCollectionEmpty( hubEventListeners ) )
        {
            ExecutorService notifier = Executors.newFixedThreadPool( hubEventListeners.size() );

            for ( final HubEventListener hubEventListener : hubEventListeners )
            {
                notifier.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            hubEventListener.onRegistrationSucceeded();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error notifying hub event listener", e );
                        }
                    }
                } );
            }

            notifier.shutdown();
        }
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public void unregisterPeer() throws HubManagerException
    {
        RegistrationManager registrationManager = new RegistrationManager( this, configManager );

        registrationManager.unregister();

        if ( !CollectionUtil.isCollectionEmpty( hubEventListeners ) )
        {
            ExecutorService notifier = Executors.newFixedThreadPool( hubEventListeners.size() );

            for ( final HubEventListener hubEventListener : hubEventListeners )
            {
                notifier.execute( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        try
                        {
                            hubEventListener.onUnregister();
                        }
                        catch ( Exception e )
                        {
                            log.error( "Error notifying hub event listener", e );
                        }
                    }
                } );
            }

            notifier.shutdown();
        }
    }


    @Override
    public String getHubDns() throws HubManagerException
    {
        Config config = getConfigDataService().getHubConfig( configManager.getPeerId() );

        if ( config != null )
        {
            return config.getHubIp();
        }
        else
        {
            return null;
        }
    }


    @Override
    public String getProducts() throws HubManagerException
    {
        try
        {
            RestResult<String> restResult = restClient.getPlain( "/rest/v1/marketplace/products/public", String.class );

            if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                return null;
            }

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                log.error( restResult.getError() );
                return null;
            }

            ProductsDto productsDto = new ProductsDto( restResult.getEntity() );

            return JsonUtil.toJson( productsDto );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( "Could not retrieve product data", e );
        }
    }


    @Override
    public void installPlugin( String url, String name, String uid ) throws HubManagerException
    {
        //Using WebClient directly here b/c we need to close it only after response is processed
        WebClient client = null;
        InputStream initialStream = null;
        try
        {
            client = RestUtil.createTrustedWebClient( url );
            File product = client.get( File.class );
            initialStream = FileUtils.openInputStream( product );
            File targetFile =
                    new File( String.format( "%s/deploy", System.getProperty( "karaf.home" ) ) + "/" + name + ".kar" );
            FileUtils.copyInputStreamToFile( initialStream, targetFile );
            initialStream.close();

            if ( isRegisteredWithHub() )
            {
                PeerProductDataDto peerProductDataDto = new PeerProductDataDto();
                peerProductDataDto.setProductId( uid );
                peerProductDataDto.setState( PeerProductDataDto.State.INSTALLED );
                peerProductDataDto.setInstallDate( new Date() );

                try
                {
                    productProcessor.updatePeerProductData( peerProductDataDto );
                }
                catch ( Exception e )
                {
                    log.error( "Failed to send plugin install command to Hub: {}", e.getMessage() );
                }
            }

            log.debug( "Product installed successfully..." );
        }
        catch ( Exception e )
        {
            throw new HubManagerException( e );
        }
        finally
        {
            RestUtil.close( client );
            SafeCloseUtil.close( initialStream );
        }
    }


    @Override
    public void uninstallPlugin( final String name, final String uid )
    {
        File file = new File( String.format( "%s/deploy", System.getProperty( "karaf.home" ) ) + "/" + name + ".kar" );

        if ( file.delete() )
        {
            log.debug( file.getName() + " is removed." );
        }

        if ( isRegisteredWithHub() )
        {
            PeerProductDataDto peerProductDataDto = new PeerProductDataDto();
            peerProductDataDto.setProductId( uid );
            peerProductDataDto.setState( PeerProductDataDto.State.REMOVE );

            try
            {
                productProcessor.deletePeerProductData( peerProductDataDto );
            }
            catch ( Exception e )
            {
                log.error( "Failed to send plugin remove command to Hub: {}", e.getMessage() );
            }
        }

        log.debug( "Product uninstalled successfully..." );
    }


    @Override
    public boolean isRegisteredWithHub()
    {
        return getHubConfiguration() != null;
    }


    @Override
    public boolean isHubReachable()
    {
        return heartbeatProcessor != null && heartbeatProcessor.isHubReachable();
    }


    @Override
    public boolean canWorkWithHub()
    {
        return isHubReachable() && isRegisteredWithHub();
    }


    @Override
    public String getPeerName()
    {
        Config config = getHubConfiguration();

        if ( config != null )
        {
            return config.getPeerName();
        }

        return null;
    }


    @Override
    public Map<String, String> getPeerInfo() throws HubManagerException
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
            throw new HubManagerException( "Could not retrieve Peer info", e );
        }

        return result;
    }


    @Override
    public Config getHubConfiguration()
    {
        return configDataService.getHubConfig( configManager.getPeerId() );
    }


    @Override
    public String getChecksum()
    {
        try
        {
            RestResult<String> restResult =
                    restClient.getPlain( "/rest/v1/marketplace/products/checksum", String.class );

            if ( restResult.getStatus() != HttpStatus.SC_OK )
            {
                log.error( restResult.getError() );
            }
            else
            {
                return restResult.getEntity();
            }
        }
        catch ( Exception e )
        {
            log.error( "Could not retrieve checksum", e );
        }

        return null;
    }


    public RestClient getRestClient()
    {
        return restClient;
    }


    @Override
    public void sendSystemConfiguration( final SystemConfDto dto )
    {
        if ( isRegisteredWithHub() )
        {
            try
            {
                String path = "/rest/v1/system-changes";

                RestResult<Object> restResult = restClient.post( path, dto, Object.class );

                log.info( "Sending Configuration of SS to Hub..." );

                if ( restResult.getStatus() == HttpStatus.SC_NO_CONTENT )
                {
                    log.info( "SS configuration sent successfully." );
                }
                else
                {
                    log.error( "Could not send SS configuration to Hub: ", restResult.getError() );
                }
            }
            catch ( Exception e )
            {
                log.error( "Could not send SS configuration to Hub", e );
            }
        }
    }


    @Override
    public String getCurrentUserEmail()
    {
        User currentUser = identityManager.getActiveUser();

        String email = currentUser.getEmail();

        log.info( "currentUser: id={}, username={}, email={}", currentUser.getId(), currentUser.getUserName(), email );

        if ( !email.contains( HUB_EMAIL_SUFFIX ) && isRegisteredWithHub() )
        {
            return getHubConfiguration().getOwnerEmail();
        }

        UserDto userDto = envUserHelper.getUserDataFromHub( StringUtils.substringBefore( email, "@" ) );

        return userDto.getEmail();
    }


    public void addListener( HubEventListener listener )
    {
        if ( listener != null )
        {
            hubEventListeners.add( listener );
        }
    }


    public void removeListener( HubEventListener listener )
    {
        if ( listener != null )
        {
            hubEventListeners.remove( listener );
        }
    }


    public void destroy()
    {
        heartbeatExecutorService.shutdown();
        peerLogsExecutor.shutdown();
        resourceHostMonitorExecutorService.shutdown();
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


    public void setMonitor( Monitor monitor )
    {
        this.monitor = monitor;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public void onContainerStateChanged( final ContainerHostInfo containerInfo, final ContainerHostState previousState,
                                         final ContainerHostState currentState )
    {

    }


    @Override
    public void onContainerHostnameChanged( final ContainerHostInfo containerInfo, final String previousHostname,
                                            final String currentHostname )
    {

    }


    @Override
    public void onContainerCreated( final ContainerHostInfo containerInfo )
    {

    }


    @Override
    public void onContainerNetInterfaceChanged( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel oldNetInterface,
                                                final HostInterfaceModel newNetInterface )
    {

    }


    @Override
    public void onContainerNetInterfaceAdded( final ContainerHostInfo containerInfo,
                                              final HostInterfaceModel netInterface )
    {

    }


    @Override
    public void onContainerNetInterfaceRemoved( final ContainerHostInfo containerInfo,
                                                final HostInterfaceModel netInterface )
    {

    }


    @Override
    public void onRhConnected( final ResourceHostInfo newRhInfo )
    {
        try
        {
            if ( isRegisteredWithHub() )
            {
                //delay to let peer register the RH

                TaskUtil.sleep( 1000 );

                log.debug( "Notifying Hub about RH connection" );

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
            if ( isRegisteredWithHub() )
            {
                log.debug( "Notifying Hub about RH disconnection" );

                peerMetricsProcessor.request();
            }
        }
        catch ( Exception e )
        {
            log.error( "Error sending peer metrics", e );
        }
    }
}