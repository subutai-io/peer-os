package io.subutai.core.peer.rest;


import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.host.Interface;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.InterfacePattern;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.PeerStatus;
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


public class RestServiceImpl implements RestService
{

    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
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
    public Response getLocalPeerInfo()
    {
        try
        {
            PeerInfo selfInfo = peerManager.getLocalPeerInfo();
            return Response.ok( jsonUtil.to( selfInfo ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating local peer info #getLocalPeerInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public String getId()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return localPeer.getId();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting local peer id#getId", e );
            return "ERROR";
        }
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
    public Response getPeerPolicy( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( peerId );
            if ( peerPolicy == null )
            {
                return Response.ok().build();
            }
            else
            {
                return Response.ok( jsonUtil.to( peerPolicy ) ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting peer policy #getPeerPolicy", e );
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


    @Override
    public Response ping()
    {
        return Response.ok().build();
    }


    @Override
    public Response processRegisterRequest( String peer )
    {
        try
        {
            // ******* Convert HexString to Byte Array ****** Decrypt data
            EncryptionTool encTool = securityManager.getEncryptionTool();
            KeyManager keyManager = securityManager.getKeyManager();

            byte[] data = HexUtil.hexStringToByteArray( peer );
            data = encTool.decrypt( data );
            //************************************************

            PeerInfo p = jsonUtil.from( new String( data ), PeerInfo.class );
            LOGGER.debug( peer );

            if ( peerManager.getPeerInfo( p.getId() ) != null )
            {
                return Response.status( Response.Status.CONFLICT )
                               .entity( String.format( "%s already registered", p.getName() ) ).build();
            }
            else
            {
                //Encrypt Local Peer
                PGPPublicKey pkey = keyManager.getRemoteHostPublicKey( p.getId(), p.getIp() );
                PeerInfo localPeer = peerManager.getLocalPeerInfo();

                if ( pkey != null )
                {
                    localPeer.setKeyPhrase( p.getKeyPhrase() );
                    String jsonData = jsonUtil.to( localPeer );
                    data = encTool.encrypt( jsonData.getBytes(), pkey, false );

                    // Save to DB
                    p.setStatus( PeerStatus.REQUESTED );
                    p.setName( String.format( "Peer %s", p.getId() ) );
                    peerManager.register( p );

                    return Response.ok( HexUtil.byteArrayToHexString( data ) ).build();
                }
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error processing register request #processRegisterRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response sendRegistrationRequest( final String peerIp )
    {
        try
        {
            String baseUrl = String.format( "https://%s:%s/cxf", peerIp, ChannelSettings.SECURE_PORT_X1 );
            WebClient client = restUtil.getTrustedWebClient( baseUrl );
            client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );
            Form form = new Form();
            form.set( "peer", jsonUtil.to( peerManager.getLocalPeerInfo() ) );


            Response response = client.path( "peer/register" ).form( form );
            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                return registerPeerCert( response );
            }
            else if ( response.getStatus() == Response.Status.CONFLICT.getStatusCode() )
            {
                String reason = response.readEntity( String.class );
                LOGGER.warn( reason );
                return Response.serverError().entity( reason ).build();
            }
            else
            {
                LOGGER.warn( "Response for registering peer: " + response.toString() );
                return Response.serverError().build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "error sending request", e );
            return Response.serverError().entity( e.toString() ).build();
        }
    }


    protected Response registerPeerCert( final Response response )
    {
        try
        {
            String responseString = response.readEntity( String.class );
            LOGGER.info( response.toString() );
            PeerInfo remotePeerInfo = jsonUtil.from( responseString, new TypeToken<PeerInfo>()
            {}.getType() );
            if ( remotePeerInfo != null )
            {
                remotePeerInfo.setStatus( PeerStatus.REQUEST_SENT );
                try
                {
                    peerManager.register( remotePeerInfo );
                }
                catch ( PeerException e )
                {
                    LOGGER.error( "Couldn't register peer", e );
                }
            }
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error registering peer certificate #registerPeerCert", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response unregisterPeer( String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            UUID id = UUID.fromString( peerId );

            boolean result = peerManager.unregister( id.toString() );
            if ( result )
            {
                //************ Delete Trust SSL Cert **************************************
                securityManager.getKeyStoreManager().removeCertFromTrusted( ChannelSettings.SECURE_PORT_X2, peerId );
                httpContextManager.reloadTrustStore();
                //***********************************************************************


                return Response.ok( "Successfully unregistered peer: " + peerId ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( Exception pe )
        {
            LOGGER.error( "Error unregistering peer #unregisterPeer", pe );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( pe.toString() ).build();
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
    public Response removeRegistrationRequest( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            UUID id = UUID.fromString( peerId );
            peerManager.unregister( id.toString() );
            return Response.status( Response.Status.NO_CONTENT ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error removing registration request #removeRegistrationRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    /* *************************************************************
     * Get Public key and save it in the local KeyServer
     */
    private String getRemotePeerPublicKey( String peerId, final String ip )
    {
        String baseUrl = String.format( "https://%s:%s/cxf", ip, ChannelSettings.SECURE_PORT_X1 );
        WebClient client = RestUtil.createTrustedWebClient( baseUrl );
        client.type( MediaType.MULTIPART_FORM_DATA ).accept( MediaType.APPLICATION_JSON );

        try
        {
            Response response = client.path( "security/keyman/getpublickeyring" ).query( "hostid", "" ).get();

            if ( response.getStatus() == Response.Status.OK.getStatusCode() )
            {
                // Get Remote peer Public Key and save in the local keystore
                String publicKeyring = response.readEntity( String.class );

                securityManager.getKeyManager().savePublicKeyRing( peerId, ( short ) 3, publicKeyring );
            }
            return peerId;
        }
        catch ( Exception ex )
        {
            return "";
        }
    }


    @Override
    public Response approveForRegistrationRequest( final String approvedPeer, final String certHEX )
    {
        try
        {
            // ******* Convert HexString to Byte Array ****** Decrypt data
            EncryptionTool encTool = securityManager.getEncryptionTool();
            KeyManager keyManager = securityManager.getKeyManager();

            byte data[] = HexUtil.hexStringToByteArray( approvedPeer );
            byte cert[] = HexUtil.hexStringToByteArray( certHEX );

            data = encTool.decrypt( data );
            cert = encTool.decrypt( cert );
            //*************************************************************

            PeerInfo p = jsonUtil.from( new String( data ), PeerInfo.class );

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
                String localPeerCert = "";

                localPeerCert =
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
    public Response updatePeer( String peer )
    {
        try
        {
            PeerInfo p = jsonUtil.from( peer, PeerInfo.class );
            p.setIp( getRequestIp() );
            p.setName( String.format( "Peer %s", p.getId() ) );
            peerManager.update( p );

            return Response.ok( jsonUtil.to( p ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating peer #updatePeer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    protected String getRequestIp()
    {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getRemoteAddr();
    }


    //*************************************************************************************
    @Override
    public Response destroyContainer( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).dispose();
            }

            //****** No ENC *******************
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error destroying container #destroyContainer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response startContainer( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).start();
            }
            //****** No ENC *******************
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error starting container #startContainer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response stopContainer( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).stop();
            }
            //****** No ENC *******************
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error stopping cotnainer #stopContainer", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response isContainerConnected( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Boolean result = localPeer.bindHost( containerId ).isConnected();

            return Response.ok( result.toString() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container status #isContainerConnected", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getContainerState( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            ContainerHostState containerHostState = localPeer.getContainerHostById( containerId ).getStatus();

            String jsonData = jsonUtil.to( containerHostState );

            return Response.ok( jsonData ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container state #getContainerState", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getTemplate( final String templateName )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Template result = localPeer.getTemplate( templateName );
            return Response.ok( jsonUtil.to( result ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting template #getTemplate", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    //*********** Quota functions ***************


    @Override
    public Response getAvailableRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available ram quota #getAvailableRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getAvailableCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available cpu quota #getAvailableCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getAvailableDiskQuota(
                    jsonUtil.<DiskPartition>from( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting available disk quota #getAvailableDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getProcessResourceUsage( final String containerId, final int processPid )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            ProcessResourceUsage processResourceUsage =
                    localPeer.getContainerHostById( containerId ).getProcessResourceUsage( processPid );
            return Response.ok( jsonUtil.to( processResourceUsage ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting processing resource usage #getProcessResourceUsage", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota #getRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getRamQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting ram quota info #getRamQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setRamQuota( ram );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final String ramQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setRamQuota( jsonUtil.<RamQuota>from( ramQuota, new TypeToken<RamQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting ram quota #setRamQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuota() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota #getCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuotaInfo( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( containerId ).getCpuQuotaInfo() ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu quota info #getCpuQuotaInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setCpuQuota( cpu );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu quota #setCpuQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuSet( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getCpuSet() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting cpu set #getCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final String containerId, final String cpuSet )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setCpuSet( jsonUtil.<Set<Integer>>from( cpuSet, new TypeToken<Set<Integer>>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting cpu set #setCpuSet", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( containerId ).getDiskQuota(
                    JsonUtil.<DiskPartition>fromJson( diskPartition, new TypeToken<DiskPartition>()
                    {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting disk quota #getDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId )
                     .setDiskQuota( jsonUtil.<DiskQuota>from( diskQuota, new TypeToken<DiskQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting disk quota #setDiskQuota", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDefaultGateway( final String containerId, final String gatewayIp )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( containerId ).setDefaultGateway( gatewayIp );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting default gateway #setDefaultGateway", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getContainerHostInfoById( final String containerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();


            return Response.ok( jsonUtil.to( localPeer.getContainerHostInfoById( containerId ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container host info by id #getContainerHostInfoById", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getReservedVnis()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getReservedVnis() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting reserved vnis #getReservedVnis", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getGateways()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getGateways() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting gateways #getGateways", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response createEnvironmentKeyPair( final String environmentId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.createEnvironmentKeyPair( environmentId );

            return Response.ok().status( Response.Status.CREATED ).build();
        }
        catch ( Exception ex )
        {
            return Response.status( Response.Status.NOT_FOUND ).build();
        }
    }


    @Override
    public Response reserveVni( final String vni )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            int vlan = localPeer.reserveVni( jsonUtil.from( vni, Vni.class ) );

            //***********CREATE PEK *************************************

            //***********************************************************

            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error reserving vni #reserveVni", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Set<Interface> getNetworkInterfaces( final InterfacePattern request )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        try
        {
            return localPeer.getNetworkInterfaces( request );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response addToTunnel( final N2NConfig config )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        try
        {
            localPeer.setupN2NConnection( config );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response removeN2NConnection( final String interfaceName, final String communityName )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        try
        {
            String address = interfaceName.replace( "n2n_", "" ).replace( "_", "." );
            localPeer.removeN2NConnection( new N2NConfig( address, interfaceName, communityName ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }
}