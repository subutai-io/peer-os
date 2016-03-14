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
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.environment.EnvironmentDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubEnvironmentProccessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentProccessor.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private ConfigManager configManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private Gson gson = RequiredDeserializer.createValidatingGson();


    public HubEnvironmentProccessor( final EnvironmentManager environmentManager, final ConfigManager hConfigManager,
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

            LOG.debug( "Getting EnvironmentData from Hub..." );

            Response r = client.get();
            byte[] encryptedContent = readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentPeerDto result = JsonUtil.fromCbor( plainContent, EnvironmentPeerDto.class );
            LOG.debug( "EnvironmentDto: " + result.toString() );
            return result;
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                e )
        {
            throw new HubPluginException( "Could not retrieve environment data", e );
        }
    }


    private void environmentBuildProcess( final EnvironmentPeerDto peerDto )
    {
        try
        {
            switch ( peerDto.getState() )
            {
                case EXCHANGE_PEK:
                    exchangePEK( peerDto );
                    break;
                case CREATE_CONTAINER:
                    createContainer( peerDto );
                    break;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
    }

    private void exchangePEK( final EnvironmentPeerDto peerDto )
    {

    }


    private void createContainer( final EnvironmentPeerDto peerDto )
    {

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
