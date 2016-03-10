package io.subutai.core.hubmanager.impl.proccessors;


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthenticationException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;

import io.subutai.core.strategy.api.Blueprint;
import io.subutai.common.environment.Node;
import io.subutai.common.gson.required.RequiredDeserializer;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.protocol.PlacementStrategy;
import io.subutai.common.util.RestUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.EnvPeerDto;
import io.subutai.hub.share.dto.EnvironmentDto;
import io.subutai.hub.share.dto.EnvironmentNodeDto;
import io.subutai.hub.share.json.JsonUtil;


public class EnvironmentProccessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentProccessor.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private ConfigManager configManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private Gson gson = RequiredDeserializer.createValidatingGson();


    public EnvironmentProccessor( final EnvironmentManager environmentManager, final ConfigManager hConfigManager,
                                  final PeerManager peerManager )
    {
        this.environmentManager = environmentManager;
        this.configManager = hConfigManager;
        this.peerManager = peerManager;
    }


    @Override
    public void proccessStateLinks( final Set<String> stateLinks ) throws HubPluginException
    {
        for ( String link : stateLinks )
        {
            // Environment Data     GET /rest/v1/environments/{environment-id}
            Matcher environmentDataMatcher = ENVIRONMENT_DATA_PATTERN.matcher( link );
            if ( environmentDataMatcher.matches() )
            {
                EnvironmentDto environmentDto = getEnvironmentData( link );
                environmentProcess( environmentDto );
            }
        }
    }


    private EnvironmentDto getEnvironmentData( String link ) throws HubPluginException
    {
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( link, configManager.getHubIp() );

            LOG.debug( "Getting EnvironmentData from Hub..." );

            Response r = client.get();
            byte[] encryptedContent = readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentDto result = JsonUtil.fromCbor( plainContent, EnvironmentDto.class );
            LOG.debug( "EnvironmentDto: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HubPluginException( "Could not retrieve environment data", e );
        }
    }


    private void environmentProcess( final EnvironmentDto environmentDto ) throws HubPluginException
    {

        if ( environmentDto.getState().equals( EnvironmentDto.State.REGISTERING_PEERS ) )
        {
            EnvironmentDto dto = processRegistering( environmentDto );
            if ( dto != null )
            {
                updateEnvironmentDto( dto, dto.getId() );
            }
        }
        else if ( environmentDto.getState().equals( EnvironmentDto.State.UNREGISTERING_PEERS ) )
        {
            EnvironmentDto dto = processUnregistering( environmentDto );
            updateEnvironmentDto( dto, dto.getId() );
        }
        else
        {
            environmentActionProcess( environmentDto );
        }
    }


    private EnvironmentDto processRegisteringViaAPI( final EnvironmentDto environmentDto ) throws HubPluginException
    {
        String localPeerId = peerManager.getLocalPeer().getId();
        for ( EnvPeerDto peerDataDto : environmentDto.getPeers() )
        {
            if ( peerDataDto.getState().equals( EnvPeerDto.PeerState.REGISTER ) && !peerDataDto.getPeerId()
                                                                                               .equals( localPeerId ) )
            {
                LOG.debug( "Sending peer register request to " + peerDataDto.getPeerId() );
                try
                {
                    peerManager.doRegistrationRequest( peerDataDto.getIpAddress(), environmentDto.getKeyPhrase() );
                    peerDataDto.setState( EnvPeerDto.PeerState.SENT );
                }
                catch ( PeerException e )
                {
                    LOG.error( "Could not sent peer register request", e.getMessage() );
                    peerDataDto.setState( EnvPeerDto.PeerState.FAILED );
                }
            }
            else if ( peerDataDto.getState().equals( EnvPeerDto.PeerState.SENT ) && peerDataDto.getPeerId()
                                                                                               .equals( localPeerId ) )
            {
                String requestPeerId = environmentDto.getInitiatorPeerId();
                try
                {
                    List<RegistrationData> registrationDatas = peerManager.getRegistrationRequests();
                    for ( RegistrationData registrationData : registrationDatas )
                    {
                        if ( registrationData.getPeerInfo().getId().equals( requestPeerId ) && registrationData
                                .getStatus().equals( RegistrationStatus.REQUESTED ) )
                        {
                            LOG.debug( "Approving register request " + requestPeerId );
                            peerManager.doApproveRequest( environmentDto.getKeyPhrase(), registrationData );
                            environmentDto.findPeer( localPeerId ).setState( EnvPeerDto.PeerState.ACCEPTED );
                        }
                    }
                }
                catch ( PeerException e )
                {
                    LOG.error( "Could not sent peer unregister request", e.getMessage() );
                    environmentDto.findPeer( localPeerId ).setState( EnvPeerDto.PeerState.FAILED );
                }
            }
        }
        return environmentDto;
    }


    private EnvironmentDto processRegistering( final EnvironmentDto environmentDto ) throws HubPluginException
    {
        String localPeerId = peerManager.getLocalPeer().getId();
        for ( EnvPeerDto peerDataDto : environmentDto.getPeers() )
        {
            if ( peerDataDto.getState().equals( EnvPeerDto.PeerState.REGISTER ) && !peerDataDto.getPeerId()
                                                                                               .equals( localPeerId ) )
            {
                LOG.debug( "Sending peer register request to " + peerDataDto.getPeerId() );
                try
                {
                    String token = getToken( "admin", "secret" );
                    WebClient client =
                            RestUtil.createTrustedWebClient( "https://127.0.0.1:8443/rest/ui/peers?sptoken=" + token );
                    client.accept( MediaType.APPLICATION_JSON );
                    client.type( MediaType.APPLICATION_FORM_URLENCODED );

                    Form form = new Form();
                    form.param( "ip", peerDataDto.getIpAddress() );
                    form.param( "key_phrase", environmentDto.getKeyPhrase() );
                    Response response = client.post( form );
                    if ( response.getStatus() == HttpStatus.SC_OK )
                    {
                        peerDataDto.setState( EnvPeerDto.PeerState.SENT );
                    }
                }
                catch ( AuthenticationException e )
                {
                    LOG.error( "Could get token.", e.getMessage() );
                }
            }
            else if ( peerDataDto.getState().equals( EnvPeerDto.PeerState.SENT ) && peerDataDto.getPeerId()
                                                                                               .equals( localPeerId ) )
            {
                String requestPeerId = environmentDto.getInitiatorPeerId();
                try
                {
                    String token = getToken( "admin", "secret" );
                    WebClient client = RestUtil.createTrustedWebClient(
                            "https://127.0.0.1:8443/rest/ui/peers/approve?sptoken=" + token );
                    client.accept( MediaType.TEXT_PLAIN );
                    client.type( MediaType.APPLICATION_FORM_URLENCODED );

                    Form form = new Form();
                    form.param( "peerId", requestPeerId );
                    form.param( "key_phrase", environmentDto.getKeyPhrase() );
                    Response response = client.put( form );
                    if ( response.getStatus() == HttpStatus.SC_OK )
                    {
                        environmentDto.findPeer( localPeerId ).setState( EnvPeerDto.PeerState.ACCEPTED );
                    }
                }
                catch ( AuthenticationException e )
                {
                    LOG.error( "Could not sent peer unregister request", e.getMessage() );
                }
            }
        }
        return environmentDto;
    }


    private EnvironmentDto processUnregisteringViaAPI( final EnvironmentDto environmentDto ) throws HubPluginException
    {

        for ( EnvPeerDto registerPeerDataDto : environmentDto.getPeers() )
        {
            LOG.debug( "Sending peer unregister request to %s", registerPeerDataDto.getPeerId() );
            if ( registerPeerDataDto.getState().equals( EnvPeerDto.PeerState.UNREGISTER ) )
            {
                try
                {
                    List<RegistrationData> registrationDatas = peerManager.getRegistrationRequests();
                    for ( RegistrationData registrationData : registrationDatas )
                    {
                        if ( registrationData.getPeerInfo().getId().equals( registerPeerDataDto.getPeerId() )
                                && registrationData.getStatus().equals( RegistrationStatus.APPROVED ) )
                        {
                            registrationData.setKeyPhrase( environmentDto.getKeyPhrase() );
                            peerManager.doUnregisterRequest( registrationData );
                            registerPeerDataDto.setState( EnvPeerDto.PeerState.UNREGISTERED );
                        }
                    }
                }
                catch ( PeerException e )
                {
                    LOG.error( "Could not unregister peer", e.getMessage() );
                    registerPeerDataDto.setState( EnvPeerDto.PeerState.FAILED );
                }
            }
        }

        return environmentDto;
    }


    private EnvironmentDto processUnregistering( final EnvironmentDto environmentDto ) throws HubPluginException
    {

        for ( EnvPeerDto envPeer : environmentDto.getPeers() )
        {
            LOG.debug( "Sending peer unregister request to %s", envPeer.getPeerId() );
            if ( envPeer.getState().equals( EnvPeerDto.PeerState.UNREGISTER ) && !envPeer.getPeerId().equals(
                    environmentDto.getInitiatorPeerId() ) )
            {
                try
                {
                    String token = getToken( "admin", "secret" );
                    WebClient client = RestUtil.createTrustedWebClient(
                            "https://127.0.0.1:8443/rest/ui/peers/unregister?sptoken=" + token );
                    client.accept( MediaType.TEXT_PLAIN );
                    client.type( MediaType.APPLICATION_FORM_URLENCODED );

                    Form form = new Form();
                    form.param( "peerId", envPeer.getPeerId() );
                    Response response = client.put( form );
                    if ( response.getStatus() == HttpStatus.SC_OK )
                    {
                        envPeer.setState( EnvPeerDto.PeerState.UNREGISTERED );
                    }
                }
                catch ( AuthenticationException e )
                {
                    LOG.error( "Could not get token", e.getMessage() );
                }
            }
        }

        return environmentDto;
    }


    private void environmentActionProcess( final EnvironmentDto environmentDto )
    {
        try
        {
            switch ( environmentDto.getState() )
            {
                case INITIALIZING:
                    createEnvironment( environmentDto );
                    break;
                case DESTROYING:
                    destroyEnvironment( environmentDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }


    private void createEnvironmentViaAPI( final EnvironmentDto environmentDto )
    {
        try
        {
            Set<Node> nodes = new HashSet<>();
            for ( EnvironmentNodeDto nodeDto : environmentDto.getNodes() )
            {
                PlacementStrategy placementStrategy = new PlacementStrategy( nodeDto.getContainerPlacementStrategy() );
                Node node = new Node( UUID.randomUUID().toString(), nodeDto.getName(), nodeDto.getTemplateName(),
                        ContainerSize.SMALL, Integer.valueOf( nodeDto.getSshGroupId() ),
                        Integer.valueOf( nodeDto.getHostsGroupId() ), nodeDto.getPeerId(), nodeDto.getHostId() );
                nodes.add( node );
            }
//            Blueprint blueprint = new Blueprint( environmentDto.getName(), nodes, sshGroupId );
            //TODO refactor after EnvironmentManagement will be ready
            //            Topology topology = new Topology(environmentDto.getName(), environmentDto.get);
            //
            //            Environment environment = environmentManager.createEnvironment( blueprint, false );
            LOG.debug( "Environment Successfully created." );
        }
        catch ( Exception e )
        {
            LOG.error( "Error creating environment #createEnvironment", e );
        }
    }


    private void createEnvironment( final EnvironmentDto environmentDto )
            throws IOException, HubPluginException, UnrecoverableKeyException, NoSuchAlgorithmException,
            KeyStoreException
    {
        LOG.debug( "Building Environment ..." );
        String oldEnvId = environmentDto.getId();
        String newEnvId;
        try
        {
            String token = getToken( "admin", "secret" );
            WebClient client =
                    RestUtil.createTrustedWebClient( "https://127.0.0.1:8443/rest/v1/environments?sptoken=" + token );
            client.accept( MediaType.APPLICATION_JSON );
            client.type( "application/json" );
            String body = environmentDto.toJsonString();
            Response responseEnv = client.post( body );
            if ( responseEnv.getStatus() == HttpStatus.SC_OK )
            {
                InputStream is = ( ( InputStream ) responseEnv.getEntity() );
                newEnvId = getStringFromInputStream( is );
            }
            else
            {
                throw new HubPluginException( "Could not create Environment." );
            }
        }
        catch ( AuthenticationException e )
        {
            throw new HubPluginException( "Could not get token." );
        }

        LOG.debug( "Environment successfully built..." );
        if ( newEnvId != null )
        {
            try
            {
                JSONObject o = new JSONObject( newEnvId );
                environmentDto.setId( o.getString( "id" ) );
            }
            catch ( JSONException e )
            {
                LOG.error( e.getMessage() );
            }
        }
        environmentDto.setState( EnvironmentDto.State.READY );
        updateEnvironmentDto( environmentDto, oldEnvId );
    }


    private void destroyEnvironment( final EnvironmentDto environmentDto )
            throws HubPluginException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        LOG.debug( "Destroying Environment ..." );
        try
        {
            String token = getToken( "admin", "secret" );

            WebClient client = RestUtil.createTrustedWebClient(
                    "https://127.0.0.1:8443/rest/v1/environments/" + environmentDto.getId() + "?sptoken=" + token );
            client.accept( MediaType.APPLICATION_JSON );
            client.type( "application/json" );
            Response responseEnv = client.delete();
            if ( responseEnv.getStatus() == HttpStatus.SC_OK )
            {
                InputStream inputStream = responseEnv.readEntity( InputStream.class );
                getStringFromInputStream( inputStream );
            }
            else
            {
                throw new HubPluginException( "Could not destroy Environment." );
            }
        }
        catch ( AuthenticationException e )
        {
            throw new HubPluginException( e.getMessage() );
        }

        LOG.debug( "Environment successfully destroyed..." );

        environmentDto.setState( EnvironmentDto.State.DISABLED );
        updateEnvironmentDto( environmentDto, environmentDto.getId() );
    }


    private void updateEnvironmentDto( EnvironmentDto environmentDto, String envId ) throws HubPluginException
    {
        LOG.debug( "Sending: " + environmentDto );
        String path = String.format( "/rest/v1/environments/%s", envId );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( path, configManager.getHubIp() );

            byte[] plainData = JsonUtil.toCbor( environmentDto );
            byte[] encryptedData = configManager.getMessenger().produce( plainData );
            Response r = client.put( encryptedData );
            if ( r.getStatus() != HttpStatus.SC_NO_CONTENT )
            {
                LOG.warn( "Unexpected response: " + r.readEntity( String.class ) );
            }
        }
        catch ( UnrecoverableKeyException | PGPException | NoSuchAlgorithmException | KeyStoreException |
                JsonProcessingException e )
        {
            throw new HubPluginException( "Could not send environment peer data.", e );
        }
    }


    private String getToken( String user, String password ) throws AuthenticationException
    {
        String loginUrl = String.format(
                "https://127.0.0.1:8443/rest/v1/identity/gettoken?username=" + user + "&password=" + password );
        WebClient webClient = RestUtil.createTrustedWebClient( loginUrl );
        Response response = webClient.get();
        if ( response.getStatus() == HttpStatus.SC_OK )
        {
            InputStream responseEntity = response.readEntity( InputStream.class );
            if ( responseEntity != null )
            {
                return getStringFromInputStream( responseEntity );
            }
            else
            {
                throw new AuthenticationException( "Could not get token." );
            }
        }
        else
        {
            throw new AuthenticationException( "Could not get token." );
        }
    }


    private byte[] readContent( Response response ) throws IOException
    {
        if ( response.getEntity() == null )
        {
            return null;
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        InputStream is = ( ( InputStream ) response.getEntity() );

        IOUtils.copy( is, bos );
        return bos.toByteArray();
    }


    private String getStringFromInputStream( InputStream is )
    {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try
        {

            br = new BufferedReader( new InputStreamReader( is ) );
            while ( ( line = br.readLine() ) != null )
            {
                sb.append( line );
            }
        }
        catch ( IOException e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            if ( br != null )
            {
                try
                {
                    br.close();
                }
                catch ( IOException e )
                {
                    LOG.error( e.getMessage() );
                }
            }
        }

        return sb.toString();
    }
}
