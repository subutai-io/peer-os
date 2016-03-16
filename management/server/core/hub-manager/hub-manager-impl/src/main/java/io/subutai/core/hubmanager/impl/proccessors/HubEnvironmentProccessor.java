package io.subutai.core.hubmanager.impl.proccessors;


import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPException;
import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.HttpStatus;

import com.google.common.collect.Sets;

import io.subutai.common.environment.CreateEnvironmentContainerGroupRequest;
import io.subutai.common.environment.CreateEnvironmentContainerResponseCollector;
import io.subutai.common.host.HostArchitecture;
import io.subutai.common.host.HostInterface;
import io.subutai.common.network.Vni;
import io.subutai.common.network.Vnis;
import io.subutai.common.peer.ContainerSize;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.PeerException;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.task.CloneRequest;
import io.subutai.common.task.CloneResponse;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.StateLinkProccessor;
import io.subutai.core.hubmanager.impl.ConfigManager;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.hub.share.dto.environment.EnvironmentNodeDto;
import io.subutai.hub.share.dto.environment.EnvironmentNodesDto;
import io.subutai.hub.share.dto.environment.EnvironmentPeerDto;
import io.subutai.hub.share.dto.network.VniDto;
import io.subutai.hub.share.json.JsonUtil;


public class HubEnvironmentProccessor implements StateLinkProccessor
{
    private static final Logger LOG = LoggerFactory.getLogger( HubEnvironmentProccessor.class.getName() );

    private static final Pattern ENVIRONMENT_DATA_PATTERN =
            Pattern.compile( "/rest/v1/environments/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})" );
    private ConfigManager configManager;
    private PeerManager peerManager;
    private EnvironmentManager environmentManager;
    private IdentityManager identityManager;


    public HubEnvironmentProccessor( final EnvironmentManager environmentManager, final ConfigManager hConfigManager,
                                     final PeerManager peerManager, final IdentityManager identityManager )
    {
        this.environmentManager = environmentManager;
        this.configManager = hConfigManager;
        this.peerManager = peerManager;
        this.identityManager = identityManager;
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
        Set<String> usedIps = Sets.newHashSet();
        String exchangeURL =
                String.format( "/rest/v1/environments/%s/exchange-pek", peerDto.getEnvironmentInfo().getId() );
        EnvironmentId environmentId = new EnvironmentId( peerDto.getEnvironmentInfo().getId() );

        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( exchangeURL, configManager.getHubIp() );

            LOG.debug( "Sending PEK to Hub ..." );

            for ( HostInterface hostInterface : peerManager.getLocalPeer().getInterfaces().getAll() )
            {
                usedIps.add( hostInterface.getIp() );
            }

            Vnis vnis = peerManager.getLocalPeer().getReservedVnis();
            for ( Vni vni: vnis.list())
            {
                VniDto vniDto = new VniDto();
                vniDto.setPeerId( peerManager.getLocalPeer().getId() );
                vniDto.setEnvironmentId( peerDto.getEnvironmentInfo().getId() );
                vniDto.setVni( vni.getVni() );
                vniDto.setVlan( vni.getVlan() );
                peerDto.getVnis().add( vniDto );
            }

            peerDto.setUsedIPs( usedIps );

            PublicKeyContainer publicKeyContainer =
                    peerManager.getLocalPeer().createPeerEnvironmentKeyPair( environmentId );

            io.subutai.hub.share.dto.PublicKeyContainer keyContainer =
                    new io.subutai.hub.share.dto.PublicKeyContainer();
            keyContainer.setKey( publicKeyContainer.getKey() );
            keyContainer.setHostId( publicKeyContainer.getHostId() );
            keyContainer.setFingerprint( publicKeyContainer.getFingerprint() );

            peerDto.setPublicKey( keyContainer );

            byte[] cborData = JsonUtil.toCbor( peerDto );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            Response r = client.post( encryptedData );

            if ( r.getStatus() == HttpStatus.SC_OK )
            {
                LOG.debug( "PEK sent successfully to Hub" );

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
            LOG.error( "Could not send PEK to Hub.", e.getMessage() );
        }
        catch ( PeerException e )
        {
            LOG.error( "Could not save signed key.", e.getMessage() );
        }
    }


    private void createContainer( final EnvironmentPeerDto peerDto )
    {
        String containerDataURL = String.format( "/rest/v1/environments/%s/container-creation-workflow",
                peerDto.getEnvironmentInfo().getId() );
        try
        {
            WebClient client = configManager.getTrustedWebClientWithAuth( containerDataURL, configManager.getHubIp() );

            Response r = client.get();
            byte[] encryptedContent = configManager.readContent( r );

            byte[] plainContent = configManager.getMessenger().consume( encryptedContent );
            EnvironmentNodesDto result = JsonUtil.fromCbor( plainContent, EnvironmentNodesDto.class );
            LOG.debug( "EnvironmentNodesDto: " + result.toString() );

            Vni vni = new Vni( peerDto.getEnvironmentInfo().getVni(), peerDto.getEnvironmentInfo().getId() );
            peerManager.getLocalPeer().reserveVni( vni );

            SubnetUtils.SubnetInfo subnetInfo =
                    new SubnetUtils( peerDto.getEnvironmentInfo().getSubnetCidr() ).getInfo();
            String maskLength = subnetInfo.getCidrSignature().split( "/" )[1];

            CreateEnvironmentContainerGroupRequest containerGroupRequest =
                    new CreateEnvironmentContainerGroupRequest( peerDto.getEnvironmentInfo().getId() );
            for ( EnvironmentNodeDto nodeDto : result.getNodes() )
            {
                ContainerSize contSize = ContainerSize.valueOf( ContainerSize.class, nodeDto.getContainerSize() );
                String ip = subnetInfo.getAllAddresses()[( nodeDto.getIpAddressOffset() )];
                try
                {
                    CloneRequest cloneRequest =
                            new CloneRequest( nodeDto.getHostId(), nodeDto.getHostName(), nodeDto.getContainerName(),
                                    ip + "/" + maskLength, peerDto.getEnvironmentInfo().getId(), peerDto.getPeerId(),
                                    peerDto.getOwnerId(), nodeDto.getTemplateName(), HostArchitecture.AMD64, contSize );

                    containerGroupRequest.addRequest( cloneRequest );
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage() );
                }
            }

            final CreateEnvironmentContainerResponseCollector containerCollector =
                    peerManager.getLocalPeer().createEnvironmentContainerGroup( containerGroupRequest );

            List<CloneResponse> cloneResponseList = containerCollector.getResponses();
            for ( CloneResponse cloneResponse : cloneResponseList )
            {
                for ( EnvironmentNodeDto nodeDto : result.getNodes() )
                {
                    if ( cloneResponse.getContainerName().equals( nodeDto.getContainerName() ) )
                    {
                        nodeDto.setIp( cloneResponse.getIp() );
                        nodeDto.setTemplateArch( cloneResponse.getTemplateArch().name() );
                        nodeDto.setAgentId( cloneResponse.getAgentId() );
                        nodeDto.setElapsedTime( cloneResponse.getElapsedTime() );
                    }
                }
            }


            byte[] cborData = JsonUtil.toCbor( result );
            byte[] encryptedData = configManager.getMessenger().produce( cborData );

            Response response = client.post( encryptedData );
            if ( response.getStatus() == HttpStatus.SC_NO_CONTENT )
            {
                LOG.debug( "Containers state sent successfully to Hub" );
            }
        }
        catch ( UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | PGPException | IOException
                | PeerException e )
        {
            LOG.error( "Could not get container creation data from Hub.", e.getMessage() );
        }
    }
}
