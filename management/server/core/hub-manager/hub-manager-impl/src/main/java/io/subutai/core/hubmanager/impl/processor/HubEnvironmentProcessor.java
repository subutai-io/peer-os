package io.subutai.core.hubmanager.impl.processor;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.UnrecoverableKeyException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.Subject;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.base.Strings;

import io.subutai.common.environment.Environment;
import io.subutai.common.network.DomainLoadBalanceStrategy;
import io.subutai.common.network.NetworkResource;
import io.subutai.common.network.ReservedNetworkResources;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerId;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.security.objects.PermissionObject;
import io.subutai.common.security.relation.RelationLinkDto;
import io.subutai.core.environment.api.exception.EnvironmentCreationException;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProcessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubEnvironmentManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.TrustDataDto;
import io.subutai.hub.share.dto.UserDto;
import io.subutai.hub.share.dto.environment.ContainerStateDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto.LogEvent;
import io.subutai.hub.share.dto.environment.EnvironmentPeerLogDto.LogType;
import io.subutai.hub.share.json.JsonUtil;


//TODO close web clients and responses
public class HubEnvironmentProcessor implements StateLinkProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentProcessor.class.getName() );

    private static final Pattern ENVIRONMENT_PEER_DATA_PATTERN = Pattern.compile(
            "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/peers/"
                    + "[a-zA-z0-9]{1,100}" );

    private final ConfigManager configManager;

    private final PeerManager peerManager;

    private final HubEnvironmentManager hubEnvironmentManager;

    private final EnvironmentUserHelper environmentUserHelper;

    private final IdentityManager identityManager;


    public HubEnvironmentProcessor( final HubEnvironmentManager hubEnvironmentManager,
                                    final ConfigManager hConfigManager, final PeerManager peerManager,
                                    final IdentityManager identityManager, CommandExecutor commandExecutor,
                                    EnvironmentUserHelper environmentUserHelper )
    {
        this.configManager = hConfigManager;

        this.peerManager = peerManager;

        this.hubEnvironmentManager = hubEnvironmentManager;

        this.environmentUserHelper = environmentUserHelper;

        this.identityManager = identityManager;
    }


    @Override
    public void processStateLinks( final Set<String> stateLinks ) throws HubPluginException
    {
        for ( String link : stateLinks )
        {
            // Environment Data     GET /rest/v1/environments/{environment-id}/peers/{peer-id}
            Matcher environmentDataMatcher = ENVIRONMENT_PEER_DATA_PATTERN.matcher( link );
            if ( environmentDataMatcher.matches() )
            {
                final EnvironmentPeerDto envPeerDto = getEnvPeerDto( link );
                UserDto userDto = getUserDataFromHub( envPeerDto.getOwnerId() );
                Boolean isTrustedUser = getUserTrustLevel( userDto.getFingerprint() );

                if ( isTrustedUser )
                {
                    if ( envPeerDto.getEnvOwnerToken() == null )
                    {
                        final Session session = identityManager.login( "token", envPeerDto.getPeerToken() );
                        Subject.doAs( session.getSubject(), new PrivilegedAction<Void>()
                        {

                            @Override
                            public Void run()
                            {
                                try
                                {
                                    User user = environmentUserHelper.handleEnvironmentOwnerCreation( envPeerDto );
                                    java.util.Calendar cal = Calendar.getInstance();
                                    cal.setTime( new Date() );
                                    cal.add( Calendar.YEAR, 3 );
                                    UserToken token =
                                            identityManager.createUserToken( user, null, null, null, 2, cal.getTime() );
                                    envPeerDto.setEnvOwnerToken( token.getFullToken() );
                                    updateEnvironmentPeerData( envPeerDto );
                                }
                                catch ( Exception ex )
                                {
                                    LOG.error( ex.getMessage() );
                                }
                                return null;
                            }
                        } );
                    }

                    final Session session = identityManager.login( "token", envPeerDto.getEnvOwnerToken() );
                    Subject.doAs( session.getSubject(), new PrivilegedAction<Void>()
                    {
                        @Override
                        public Void run()
                        {
                            try
                            {
                                environmentBuildProcess( envPeerDto );
                            }
                            catch ( Exception ex )
                            {
                                LOG.error( ex.getMessage() );
                            }
                            return null;
                        }
                    } );
                }
                else
                {
                    String msg = "User is not trusted.";
                    hubEnvironmentManager.sendLogToHub( envPeerDto, msg, null, LogEvent.HUB, LogType.ERROR, null );
                }
            }
        }
    }


    private EnvironmentPeerDto getEnvPeerDto( String link ) throws HubPluginException
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Getting EnvironmentPeerDto from Hub..." );

            Response response = client.get();

            client.close();

            byte[] encryptedContent = configManager.readContent( response );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

            EnvironmentPeerDto result = JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );

            LOG.debug( "Response: {} - {}", response.getStatus(), response.getStatusInfo().getReasonPhrase() );

            return result;
        }
        catch ( Exception e )
        {
            throw new HubPluginException( "Could not retrieve environment peer data", e );
        }
    }


    private void environmentBuildProcess( final EnvironmentPeerDto peerDto )
    {
        try
        {
            switch ( peerDto.getState() )
            {
                case EXCHANGE_INFO:
                    infoExchange( peerDto );
                    break;
                case RESERVE_NETWORK:
                    reserveNetwork( peerDto );
                    break;
                case SETUP_TUNNEL:
                    setupTunnel( peerDto );
                    break;
                case BUILD_CONTAINER:
                    buildContainers( peerDto );
                    break;
                case CONFIGURE_CONTAINER:
                    configureContainer( peerDto );
                    break;
                case CHANGE_CONTAINER_STATE:
                    controlContainer( peerDto );
                    break;
                case CONFIGURE_DOMAIN:
                    configureDomain( peerDto );
                    break;
                case DELETE_PEER:
                    deletePeer( peerDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    private void infoExchange( EnvironmentPeerDto peerDto )
    {
        EnvironmentInfoDto environmentInfoDto = peerDto.getEnvironmentInfo();
        String exchangeURL = String.format( "/rest/v1/environments/%s/exchange-info", environmentInfoDto.getId() );

        EnvironmentId environmentId = new EnvironmentId( environmentInfoDto.getId() );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( exchangeURL, configManager.getHubIp() );

            // TODO identify for future do we need envKeyId (or do we need keyId for {@link RelationLinkDto})
            RelationLinkDto envLink =
                    new RelationLinkDto( environmentInfoDto.getId(), Environment.class.getSimpleName(),
                            PermissionObject.EnvironmentManagement.getName(), "" );
            peerDto = hubEnvironmentManager.getReservedNetworkResource( peerDto );
            peerDto.setPublicKey( hubEnvironmentManager.createPeerEnvironmentKeyPair( envLink ) );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );
            client.close();
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                LOG.debug( "Collected data successfully sent to Hub" );
                byte[] encryptedContent = configManager.readContent( r );
                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
                EnvironmentPeerDto buildDtoResponse = JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );

                PGPPublicKeyRing signedKey = PGPKeyUtil.readPublicKeyRing( buildDtoResponse.getPublicKey().getKey() );
                peerManager.getLocalPeer().updatePeerEnvironmentPubKey( environmentId, signedKey );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            String mgs = "Could not send exchange data to Hub.";
            hubEnvironmentManager
                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
        catch ( PeerException e )
        {
            String mgs = "Could not save signed key.";
            hubEnvironmentManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
        catch ( EnvironmentCreationException e )
        {
            String mgs = "Environment creation exception";
            hubEnvironmentManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
    }


    private void reserveNetwork( EnvironmentPeerDto peerDto )
    {
        try
        {
            hubEnvironmentManager.reserveNetworkResource( peerDto );
            peerDto = hubEnvironmentManager.setupP2P( peerDto );
            updateEnvironmentPeerData( peerDto );
        }
        catch ( EnvironmentCreationException e )
        {
            LOG.error( e.getMessage() );
        }
    }


    private void setupTunnel( EnvironmentPeerDto peerDto )
    {
        String setupTunnelDataURL = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client =
                    configManager.getTrustedWebClientWithAuth( setupTunnelDataURL, configManager.getHubIp() );
            Response r = client.get();
            client.close();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentDto environmentDto = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
            peerDto.setSetupTunnel( false );
            hubEnvironmentManager.setupTunnel( peerDto, environmentDto );
            updateEnvironmentPeerData( peerDto );
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            String mgs = "Could not get environment data from Hub.";
            hubEnvironmentManager
                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
    }


    private void buildContainers( EnvironmentPeerDto peerDto )
    {
        String containerDataURL = String.format( "/rest/v1/environments/%s/container-build-workflow",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );
            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentNodesDto envNodes = JsonUtil.fromCbor( plainContent, EnvironmentNodesDto.class );

            hubEnvironmentManager.prepareTemplates( peerDto, envNodes, peerDto.getEnvironmentInfo().getId() );

            EnvironmentNodesDto updatedNodes = hubEnvironmentManager.cloneContainers( peerDto, envNodes );

            byte[] cborData = JsonUtil.toCbor( updatedNodes );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = client.put( encryptedData );
            client.close();

            LOG.debug( "response.status: {}", response.getStatus() );

            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "env_via_hub: Environment successfully build!!!" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not get container creation data from Hub.";

            hubEnvironmentManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );

            LOG.error( mgs, e );
        }
    }


    private void configureContainer( EnvironmentPeerDto peerDto )
    {
        String configContainer = String.format( "/rest/v1/environments/%s/container-configuration",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );

            peerDto = hubEnvironmentManager.configureSsh( peerDto, environmentDto );
            hubEnvironmentManager.configureHash( peerDto, environmentDto );

            WebClient clientUpdate =
                    configManager.getTrustedWebClientWithAuth( configContainer, configManager.getHubIp() );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            Response response = clientUpdate.put( encryptedData );
            clientUpdate.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "SSH configuration successfully done" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not configure SSH/Hash";
            LOG.error( mgs, e );
        }
    }


    private void controlContainer( EnvironmentPeerDto peerDto )
    {
        String controlContainerPath =
                String.format( "/rest/v1/environments/%s/peers/%s/container", peerDto.getEnvironmentInfo().getId(),
                        peerDto.getPeerId() );
        LocalPeer localPeer = peerManager.getLocalPeer();

        EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );
        if ( environmentDto != null )
        {
            for ( EnvironmentNodesDto nodesDto : environmentDto.getNodes() )
            {
                PeerId peerId = new PeerId( nodesDto.getPeerId() );
                EnvironmentId envId = new EnvironmentId( nodesDto.getEnvironmentId() );
                if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
                {
                    for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                    {
                        ContainerId containerId =
                                new ContainerId( nodeDto.getContainerId(), nodeDto.getHostName(), peerId, envId );
                        try
                        {
                            if ( nodeDto.getState().equals( ContainerStateDto.STOPPING ) )
                            {
                                localPeer.stopContainer( containerId );
                                nodeDto.setState( ContainerStateDto.STOPPED );
                            }
                            if ( nodeDto.getState().equals( ContainerStateDto.STARTING ) )
                            {
                                localPeer.startContainer( containerId );
                                nodeDto.setState( ContainerStateDto.RUNNING );
                            }
                            if ( nodeDto.getState().equals( ContainerStateDto.ABORTING ) )
                            {
                                localPeer.destroyContainer( containerId );
                                nodeDto.setState( ContainerStateDto.FROZEN );
                            }
                        }
                        catch ( PeerException e )
                        {
                            String mgs = "Could not change container state";
                            hubEnvironmentManager
                                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.CONTAINER, LogType.ERROR,
                                            containerId.getId() );
                            LOG.error( mgs, e );
                        }
                    }

                    try
                    {
                        WebClient clientUpdate = configManager
                                .getTrustedWebClientWithAuth( controlContainerPath, configManager.getHubIp() );

                        byte[] cborData = JsonUtil.toCbor( nodesDto );
                        byte[] encryptedData = configManager.getMessenger().produce( cborData );

                        Response response = clientUpdate.put( encryptedData );
                        clientUpdate.close();
                        if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
                        {
                            LOG.debug( "Container successfully updated" );
                        }
                    }
                    catch ( Exception e )
                    {
                        String mgs = "Could not send containers state to hub";
                        hubEnvironmentManager
                                .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR,
                                        null );
                        LOG.error( mgs, e );
                    }
                }
            }
        }
    }


    private void configureDomain( EnvironmentPeerDto peerDto )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        String domainUpdatePath =
                String.format( "/rest/v1/environments/%s/peers/%s/domain", peerDto.getEnvironmentInfo().getId(),
                        peerDto.getPeerId() );
        try
        {
            //TODO balanceStrategy should come from HUB
            EnvironmentDto environmentDto = getEnvironmentDto( peerDto.getEnvironmentInfo().getId() );
            boolean assign = !Strings.isNullOrEmpty( env.getDomainName() );
            assert environmentDto != null;
            if ( assign )
            {
                DomainLoadBalanceStrategy balanceStrategy = DomainLoadBalanceStrategy.LOAD_BALANCE;
                localPeer.setVniDomain( env.getVni(), env.getDomainName(), balanceStrategy, env.getSslCertPath() );
                for ( EnvironmentNodesDto nodesDto : environmentDto.getNodes() )
                {
                    if ( nodesDto.getPeerId().equals( localPeer.getId() ) )
                    {
                        for ( EnvironmentNodeDto nodeDto : nodesDto.getNodes() )
                        {
                            try
                            {
                                localPeer.addIpToVniDomain( nodeDto.getIp(), env.getVni() );
                            }
                            catch ( Exception e )
                            {
                                LOG.error( "Could not add container IP to domain: " + nodeDto.getContainerName() );
                            }
                        }
                    }
                }
            }
            else
            {
                localPeer.removeVniDomain( env.getVni() );
            }

            WebClient clientUpdate =
                    configManager.getTrustedWebClientWithAuth( domainUpdatePath, configManager.getHubIp() );
            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = clientUpdate.put( encryptedData );
            clientUpdate.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Domain configuration successfully done" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not configure domain name";
            hubEnvironmentManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
    }


    private void deletePeer( EnvironmentPeerDto peerDto )
    {
        String urlFormat = "/rest/v1/environments/%s/peers/%s";

        String containerDestroyStateURL =
                String.format( urlFormat, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId() );

        LocalPeer localPeer = peerManager.getLocalPeer();

        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();

        try
        {
            EnvironmentId envId = new EnvironmentId( env.getId() );

            localPeer.cleanupEnvironment( envId );
            ReservedNetworkResources reservedNetworkResources = localPeer.getReservedNetworkResources();
            for ( NetworkResource networkResource : reservedNetworkResources.getNetworkResources() )
            {
                if ( networkResource.getEnvironmentId().equals( env.getId() ) )
                {
                    throw new Exception( "Environment network resources are not cleaned yet." );
                }
            }

            environmentUserHelper.handleEnvironmentOwnerDeletion( peerDto );

            WebClient client =
                    configManager.getTrustedWebClientWithAuth( containerDestroyStateURL, configManager.getHubIp() );

            Response response = client.delete();
            client.close();
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Environment data cleaned successfully" );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not clean environment";
            hubEnvironmentManager.sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.SUBUTAI, LogType.ERROR, null );
            LOG.error( mgs, e );
        }
    }


    private EnvironmentDto getEnvironmentDto( String envId )
    {
        String envDataPath = String.format( "/rest/v1/environments/%s", envId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( envDataPath, configManager.getHubIp() );
            Response r = client.get();
            client.close();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            return JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
        }
        catch ( Exception e )
        {
            LOG.error( "Could not get environment data from Hub", e );
        }
        return null;
    }


    private void updateEnvironmentPeerData( EnvironmentPeerDto peerDto )
    {
        try
        {
            String envPeerDataUrl =
                    String.format( "/rest/v1/environments/%s/peers/%s", peerDto.getEnvironmentInfo().getId(),
                            peerManager.getLocalPeer().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerDataUrl, configManager.getHubIp() );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.put( encryptedData );
            client.close();
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                String mgs = "Environment peer data successfully sent to hub";
                hubEnvironmentManager.sendLogToHub( peerDto, mgs, null, LogEvent.REQUEST_TO_HUB, LogType.DEBUG, null );
                LOG.debug( mgs );
            }
        }
        catch ( Exception e )
        {
            String mgs = "Could not sent environment peer data to hub.";
            hubEnvironmentManager
                    .sendLogToHub( peerDto, mgs, e.getMessage(), LogEvent.REQUEST_TO_HUB, LogType.ERROR, null );
            LOG.error( mgs, e.getMessage() );
        }
    }


    private UserDto getUserDataFromHub( String userId )
    {
        String path = "/rest/v1/users/" + userId;

        UserDto userDto = null;

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response r = client.get();
            client.close();
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                byte[] encryptedContent = configManager.readContent( r );

                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

                userDto = JsonUtil.fromCbor( plainContent, UserDto.class );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error to get user data: ", e );
        }

        return userDto;
    }


    private Boolean getUserTrustLevel( String fingerprint )
    {
        String path = "/rest/v1/keyserver/hub/trust/" + fingerprint;
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            Response r = client.get();
            client.close();
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                byte[] encryptedContent = configManager.readContent( r );

                byte[] plainContent = configManager.getMessenger().consume( encryptedContent );

                TrustDataDto userTrustDto = JsonUtil.fromCbor( plainContent, TrustDataDto.class );
                if ( userTrustDto.getTrustLevel().equals( TrustDataDto.TrustLevel.FULL ) )
                {
                    return true;
                }
                return false;
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error to get user data: ", e );
        }
        return false;
    }
}
