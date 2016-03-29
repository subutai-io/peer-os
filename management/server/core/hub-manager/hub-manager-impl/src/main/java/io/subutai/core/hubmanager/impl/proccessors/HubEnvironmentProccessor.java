package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubEnvironmentManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentInfoDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubEnvironmentProccessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentProccessor.class.getName() );

    private static final Pattern ENVIRONMENT_PEER_DATA_PATTERN = Pattern.compile(
            "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})/peers/"
                    + "[a-zA-z0-9]{1,100}" );

    private final ConfigManager configManager;

    private final PeerManager peerManager;

    private final HubEnvironmentManager hubEnvironmentManager;

    private final CommandExecutor commandExecutor;

    private final EnvironmentUserHelper environmentUserHelper;


    public HubEnvironmentProccessor( final HubEnvironmentManager hubEnvironmentManager,
                                     final ConfigManager hConfigManager, final PeerManager peerManager,
                                     CommandExecutor commandExecutor, EnvironmentUserHelper environmentUserHelper )
    {
        this.configManager = hConfigManager;

        this.peerManager = peerManager;

        this.hubEnvironmentManager = hubEnvironmentManager;

        this.commandExecutor = commandExecutor;

        this.environmentUserHelper = environmentUserHelper;
    }


    @Override
    public void proccessStateLinks( final Set<String> stateLinks ) throws HubPluginException
    {
        for ( String link : stateLinks )
        {
            // Environment Data     GET /rest/v1/environments/{environment-id}/peers/{peer-id}
            Matcher environmentDataMatcher = ENVIRONMENT_PEER_DATA_PATTERN.matcher( link );
            if ( environmentDataMatcher.matches() )
            {
                EnvironmentPeerDto envPeerDto = getEnvPeerDto( link );
                environmentBuildProcess( envPeerDto );
            }
        }
    }


    private EnvironmentPeerDto getEnvPeerDto( String link ) throws HubPluginException
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Getting Environment peer data from Hub..." );

            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentPeerDto result = JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );

            LOG.debug( "EnvironmentPeerDto: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
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
                case SETUP_P2P:
                    setupP2P( peerDto );
                    break;
                case SETUP_TUNNEL:
                    setupTunnel( peerDto );
                    break;
                case BUILD_CONTAINER:
                    buildContainers( peerDto );
                    environmentUserHelper.handleEnvironmentOwner( peerDto );
                    break;
                case CONFIGURE_SSH:
                    configureContainer( peerDto );
                    break;
                case DESTROY_CONTAINER:
                    destroyContainers( peerDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    private void infoExchange( final EnvironmentPeerDto peerDto )
    {
        String exchangeURL =
                String.format( "/rest/v1/environments/%s/exchange-info", peerDto.getEnvironmentInfo().getId() );

        EnvironmentId environmentId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( exchangeURL, configManager.getHubIp() );

            LOG.debug( "env_via_hub: Collecting reserved VNIs..." );
            peerDto.setVnis( hubEnvironmentManager.getReservedVnis() );

            LOG.debug( "env_via_hub: Collecting used IPs..." );
            peerDto.setUsedIPs( hubEnvironmentManager.getTunnelNetworks() );

            LOG.debug( "env_via_hub: Collecting reserved gateways..." );
            peerDto.setGateways( hubEnvironmentManager.getReservedGateways() );

            LOG.debug( "env_via_hub: Generating PEK..." );
            peerDto.setPublicKey( hubEnvironmentManager.createPeerEnvironmentKeyPair( environmentId ) );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );

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
            LOG.error( "Could not send collected data to Hub.", e.getMessage() );
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not save signed key.", e.getMessage() );
        }
    }


    private void setupP2P( EnvironmentPeerDto peerDto )
    {
        LOG.debug( "env_via_hub: Setup VNI..." );
        hubEnvironmentManager.setupVNI( peerDto );

        LOG.debug( "env_via_hub: Setup P2P..." );
        peerDto = hubEnvironmentManager.setupP2P( peerDto );

        updateEnvironmentPeerData( peerDto );
    }


    private void setupTunnel( EnvironmentPeerDto peerDto )
    {
        String setupTunnelDataURL = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client =
                    configManager.getTrustedWebClientWithAuth( setupTunnelDataURL, configManager.getHubIp() );
            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentDto environmentDto = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );

            LOG.debug( "env_via_hub: Setup tunnel..." );
            try
            {
                hubEnvironmentManager.setupTunnel( environmentDto );
                peerDto.setSetupTunnel( true );
            }
            catch ( ExecutionException | InterruptedException e )
            {
                LOG.error( "Problems setting up tunnel", e );
                peerDto.setSetupTunnel( false );
            }
            updateEnvironmentPeerData( peerDto );
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            LOG.error( "Could not get environment data from Hub.", e.getMessage() );
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

            LOG.debug( "env_via_hub: Prepare templates..." );
            hubEnvironmentManager.prepareTemplates( peerDto, envNodes );

            LOG.debug( "env_via_hub: Clone containers..." );
            EnvironmentNodesDto updatedNodes = hubEnvironmentManager.cloneContainers( peerDto, envNodes );

            setupVEHS( updatedNodes, peerDto );

            byte[] cborData = JsonUtil.toCbor( updatedNodes );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response response = client.put( encryptedData );

            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "env_via_hub: Environment successfully build!!!" );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            LOG.error( "Could not get container creation data from Hub.", e.getMessage() );
        }
    }


    private void configureContainer( EnvironmentPeerDto peerDto )
    {
        String configContainer = String.format( "/rest/v1/environments/%s/container-configuration",
                peerDto.getEnvironmentInfo().getId() );
        String envDataURL = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( envDataURL, configManager.getHubIp() );
            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentDto environmentDto = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );

            hubEnvironmentManager.configureSsh( peerDto, environmentDto );
            hubEnvironmentManager.configureHash( environmentDto );

            WebClient clientUpdate =
                    configManager.getTrustedWebClientWithAuth( configContainer, configManager.getHubIp() );
            Response response = clientUpdate.put( null );
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "SSH configuration successfully done" );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Could not configure SSH/Hash", e );
        }
    }

    private EnvironmentNodesDto setupVEHS( final EnvironmentNodesDto updatedNodes, EnvironmentPeerDto peerDto )
    {
        String pull = "bash /pullMySite.sh %s %s %s \"%s\" &";

        String cloneCmd = "echo %s %s %s %s > /tmp/params";

        JSONObject jsonpObject = null;
        String githubProjectUrl = "";
        String githubUserName = "";
        String githubPassword = "";
        String githubProjectOwner = "";
        String state = "";
        String dns = "";

        String conf =
                "echo " + "'# put this into /var/lib/apps/subutai/current/nginx-includes with name like 'blabla.conf'\n"
                        + "upstream %s-upstream {\n" + "#Add new host here\n" + "server %s;\n" + "\n" + "}\n" + "\n"
                        + "server{\n" + "listen 80;\n" + "server_name %s;\n" + "\n" + "location / {\n"
                        + "proxy_pass http://%s-upstream/;\n" + "proxy_set_header X-Real-IP $remote_addr;\n"
                        + "proxy_set_header Host $http_host;\n"
                        + "proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;\n" + "}\n" + "}\n'"
                        + " > /var/lib/apps/subutai/current/nginx-includes/1000.conf";


        List<EnvironmentNodeDto> nodes = updatedNodes.getNodes();
        for ( EnvironmentNodeDto environmentNodeDto : nodes )
        {

            try
            {
                jsonpObject = new JSONObject( peerDto.getEnvironmentInfo().getVEHS() );
                githubProjectUrl = jsonpObject.getString( "githubProjectUrl" );
                githubUserName = jsonpObject.getString( "githubUserName" );
                githubPassword = jsonpObject.getString( "githubPassword" );
                githubProjectOwner = jsonpObject.getString( "githubProjectOwner" );
                state = jsonpObject.getString( "state" );
                dns = jsonpObject.getString( "dns" );

                pull = String.format( pull, githubProjectUrl, githubProjectOwner, githubUserName, githubPassword );
            }
            catch ( JSONException e )
            {
                e.printStackTrace();
            }


            try
            {
                UUID.fromString( environmentNodeDto.getContainerId() );
            }
            catch ( Exception e )
            {
                if ( state.equals( "DEPLOY" ) )
                {
                    String cmd = String.format( cloneCmd, githubProjectUrl, githubProjectOwner, githubUserName,
                            githubPassword );
                    execute( environmentNodeDto.getContainerId(), cmd );

                    execute( environmentNodeDto.getHostId(), "mkdir -p /var/lib/apps/subutai/current/nginx-includes/" );

                    execute( environmentNodeDto.getHostId(),
                            String.format( conf, dns, environmentNodeDto.getIp(), dns, dns ) );

                    execute( environmentNodeDto.getHostId(), "systemctl restart *nginx*" );

                    JSONObject jsonObject = new JSONObject();
                    try
                    {
                        jsonObject.put( "param", "status" );
                        jsonObject.put( "status", "READY" );
                    }
                    catch ( JSONException e1 )
                    {
                        e1.printStackTrace();
                    }
                    updatedNodes.setVEHS( jsonObject.toString() );
                }
                else if ( state.equals( "VERIFY_CHECKSUM" ) )
                {
                    JSONObject jsonObject = new JSONObject();
                    try
                    {
                        jsonObject.put( "param", "checksum" );
                        CommandResult result =
                                execute( environmentNodeDto.getContainerId(), "bash /checksum.sh  /var/www/" );

                        jsonObject.put( "checksum", result.getStdOut().toString() );

                        updatedNodes.setVEHS( jsonObject.toString() );
                    }
                    catch ( JSONException e1 )
                    {
                        e1.printStackTrace();
                    }
                }
            }
        }

        return updatedNodes;
    }


    private CommandResult execute( String hostId, String cmd )
    {
        boolean exec = true;
        int tryCount = 0;
        CommandResult result = null;

        while ( exec )
        {
            tryCount++;
            exec = tryCount > 3 ? false : true;
            try
            {
                result = commandExecutor.execute( hostId, new RequestBuilder( cmd ) );
                exec = false;
                return result;
            }
            catch ( CommandException e )
            {
                e.printStackTrace();
            }

            try
            {
                Thread.sleep( 1000 );
            }
            catch ( InterruptedException e )
            {
                e.printStackTrace();
            }
        }

        return null;
    }


    private void destroyContainers( EnvironmentPeerDto peerDto )
    {
        String containerDestroyStateURL =
                String.format( "/rest/v1/environments/%s/destroy", peerDto.getEnvironmentInfo().getId() );

        LocalPeer localPeer = peerManager.getLocalPeer();
        EnvironmentInfoDto env = peerDto.getEnvironmentInfo();
        try
        {
            localPeer.cleanupEnvironment( new EnvironmentId( env.getId() ) );
            localPeer.removePeerEnvironmentKeyPair( new EnvironmentId( env.getId() ) );

            WebClient client =
                    configManager.getTrustedWebClientWithAuth( containerDestroyStateURL, configManager.getHubIp() );
            Response response = client.put( null );
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Container destroyed successfully" );
            }
        }
        catch ( Exception e )
        {

            LOG.error( "Could not destroy container", e );
        }
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
            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                LOG.debug( "Environment peer data successfully sent to hub" );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Could not sent environment peer data to hub.", e.getMessage() );
        }
    }
}
