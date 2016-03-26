package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.hubmanager.impl.HubEnvironmentManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubEnvironmentProccessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentProccessor.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private ConfigManager configManager;
    private PeerManager peerManager;
    private HubEnvironmentManager hubEnvironmentManager;


    public HubEnvironmentProccessor( final HubEnvironmentManager hubEnvironmentManager,
                                     final ConfigManager hConfigManager, final PeerManager peerManager )
    {
        this.configManager = hConfigManager;
        this.peerManager = peerManager;
        this.hubEnvironmentManager = hubEnvironmentManager;
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
                case BUILD_CONTAINER:
                    buildContainers( peerDto );
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


    private void buildContainers( EnvironmentPeerDto peerDto )
    {
        String containerDataURL = String.format( "/rest/v1/environments/%s/container-build-workflow",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            LOG.debug( "env_via_hub: Setup VNI..." );
            hubEnvironmentManager.setupVNI( peerDto );

            LOG.debug( "env_via_hub: Setup P2P..." );
            peerDto = hubEnvironmentManager.setupP2P( peerDto );

            updateEnvironmentPeerData( peerDto );

            WebClient client = configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );
            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );
            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentNodesDto envNodes = JsonUtil.fromCbor( plainContent, EnvironmentNodesDto.class );

            LOG.debug( "env_via_hub: Prepare templates..." );
            hubEnvironmentManager.prepareTemplates( peerDto, envNodes );

            LOG.debug( "env_via_hub: Clone containers..." );
            EnvironmentNodesDto updatedNodes = hubEnvironmentManager.cloneContainers( peerDto, envNodes );

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


    private void updateEnvironmentPeerData( EnvironmentPeerDto peerDto )
    {
        try
        {
            String envPeerDataUrl = String.format( "/rest/v1/environments/%s", peerDto.getEnvironmentInfo().getId() );
            WebClient client = configManager.getTrustedWebClientWithAuth( envPeerDataUrl, configManager.getHubIp() );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );
            Response r = client.post( encryptedData );
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
