package io.subutai.core.peer.rest.ui;


import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.*;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.utils.io.HexUtil;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.http.manager.api.HttpContextManager;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;
import io.subutai.core.security.api.crypto.EncryptionTool;
import io.subutai.core.security.api.crypto.KeyManager;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class RestServiceImpl implements RestService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private PeerManager peerManager;
    private HttpContextManager httpContextManager;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected RestUtil restUtil = new RestUtil();
    private SecurityManager securityManager;


    public RestServiceImpl( final PeerManager peerManager, HttpContextManager httpContextManager,
                            SecurityManager securityManager )
    {
        this.peerManager = peerManager;
        this.httpContextManager = httpContextManager;
        this.securityManager = securityManager;
    }



    @Override
    public Response processRegisterRequest( String ip, String KeyPhrase )
    //public Response processRegisterRequest( String peer )
    {

        try
        {
            // ******* Convert HexString to Byte Array ****** Decrypt data
            EncryptionTool encTool = securityManager.getEncryptionTool();
            KeyManager keyManager = securityManager.getKeyManager();

            PeerInfo p = peerManager.getLocalPeerInfo();
            p.setKeyPhrase( KeyPhrase );
            PGPPublicKey pkey = keyManager.getRemoteHostPublicKey( p.getId(), ip );

            //************************************************


            PeerInfo localPeer = peerManager.getLocalPeerInfo();

            if ( pkey != null )
            {
                localPeer.setKeyPhrase( p.getKeyPhrase() );
                String jsonData = jsonUtil.to( localPeer );
                byte[] data = encTool.encrypt( jsonData.getBytes(), pkey, false );

                // Save to DB
                p.setStatus( PeerStatus.REQUESTED );
                p.setName( String.format( "Peer %s", p.getId() ) );
                peerManager.register( p );

                return Response.ok( HexUtil.byteArrayToHexString( data ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error processing register request #processRegisterRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }

        return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
    }

    @Override
    public Response getRegisteredPeers()
    {
        try
        {
            return Response.ok( jsonUtil.to( peerManager.getPeerInfos() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting registered peers #getRegisteredPeers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response rejectForRegistrationRequest( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            PeerInfo p = peerManager.getPeerInfo( peerId );
            p.setStatus( PeerStatus.REJECTED );
            peerManager.update( p );

            return Response.noContent().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error rejecting registration request #rejectForRegistrationRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response approveForRegistrationRequest( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            PeerInfo p = peerManager.getPeerInfo( peerId );
            //PeerInfo selfPeer = peerManager.getLocalPeerInfo();
            String cert = securityManager.getKeyStoreManager()
                    .exportCertificate( ChannelSettings.SECURE_PORT_X2, "" );

            EncryptionTool encTool = securityManager.getEncryptionTool();
            KeyManager keyManager = securityManager.getKeyManager();

            if ( p.getKeyPhrase().equals( ( peerManager.getPeerInfo( p.getId() ).getKeyPhrase() ) ) )
            {
                p.setStatus( PeerStatus.APPROVED );
                peerManager.update( p );

                //adding remote repository
                ManagementHost managementHost = peerManager.getLocalPeer().getManagementHost();
                managementHost.addRepository( p.getIp() );

                //************ Save Trust SSL Cert **************************************
                String rootCertPx2 = new String( cert );

                securityManager.getKeyStoreManager()
                        .importCertAsTrusted( ChannelSettings.SECURE_PORT_X2, p.getId(), rootCertPx2 );
                //***********************************************************************

                //************ Export Current Cert **************************************
                String localPeerCert =
                        securityManager.getKeyStoreManager().exportCertificate( ChannelSettings.SECURE_PORT_X2, "" );

                httpContextManager.reloadTrustStore();
                //***********************************************************************


                PGPPublicKey pkey = keyManager.getPublicKey( p.getId() ); //Get PublicKey from KeyServer
                byte certRes[] = encTool.encrypt( localPeerCert.getBytes(), pkey, false );

                return Response.ok( HexUtil.byteArrayToHexString( certRes ) ).build();
            }
            else
            {
                return Response.status( Response.Status.FORBIDDEN ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving registration request #approveForRegistrationRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

    @Override
    public Response getRegisteredPeerInfo( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            PeerInfo peerInfo = peerManager.getPeer( peerId ).getPeerInfo();
            return Response.ok( jsonUtil.to( peerInfo ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting peer info #getRegisteredPeerInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }

}