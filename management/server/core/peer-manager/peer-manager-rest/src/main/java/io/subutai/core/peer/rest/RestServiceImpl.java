package io.subutai.core.peer.rest;


import java.security.KeyStore;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.metric.ProcessResourceUsage;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.peer.PeerStatus;
import io.subutai.common.protocol.Template;
import io.subutai.common.quota.DiskPartition;
import io.subutai.common.quota.DiskQuota;
import io.subutai.common.quota.RamQuota;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreManager;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.util.UUIDUtil;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.ManagementHost;
import io.subutai.core.peer.api.PeerManager;



public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private PeerManager peerManager;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected RestUtil restUtil = new RestUtil();


    public RestServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
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
            return localPeer.getId().toString();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( UUID.fromString( peerId ) );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

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
            PeerInfo p = jsonUtil.from( peer, PeerInfo.class );
            LOGGER.debug( peer );

            if ( peerManager.getPeerInfo( p.getId() ) != null )
            {
                return Response.status( Response.Status.CONFLICT )
                               .entity( String.format( "%s already registered", p.getName() ) ).build();
            }

            p.setStatus( PeerStatus.REQUESTED );
            p.setName( String.format( "Peer %s", p.getId() ) );

            peerManager.register( p );
            return Response.ok( jsonUtil.to( peerManager.getLocalPeerInfo() ) ).build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

            UUID id = UUID.fromString( peerId );

            boolean result = peerManager.unregister( id.toString() );
            if ( result )
            {
                //************ Delete Trust SSL Cert **************************************
                KeyStore keyStore;
                KeyStoreData keyStoreData;
                KeyStoreManager keyStoreManager;

                keyStoreData = new KeyStoreData();
                keyStoreData.setupTrustStorePx2();
                keyStoreData.setAlias( peerId );

                keyStoreManager = new KeyStoreManager();
                keyStore = keyStoreManager.load( keyStoreData );

                keyStoreManager.deleteEntry( keyStore, keyStoreData );
                //***********************************************************************

                //sslContextFactory.reloadTrustStore();

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
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

            PeerInfo p = peerManager.getPeerInfo( UUID.fromString( peerId ) );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

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


    @Override
    public Response approveForRegistrationRequest( final String approvedPeer, final String rootCertPx2 )
    {
        try
        {


            PeerInfo p = jsonUtil.from( approvedPeer, PeerInfo.class );
            p.setStatus( PeerStatus.APPROVED );
            peerManager.update( p );


            //adding remote repository
            ManagementHost managementHost = peerManager.getLocalPeer().getManagementHost();
            managementHost.addRepository( p.getIp() );

            //************ Save Trust SSL Cert **************************************
            KeyStore keyStore;
            KeyStoreData keyStoreData;
            KeyStoreManager keyStoreManager;

            keyStoreData = new KeyStoreData();
            keyStoreData.setupTrustStorePx2();
            keyStoreData.setHEXCert( rootCertPx2 );
            keyStoreData.setAlias( p.getId().toString() );

            keyStoreManager = new KeyStoreManager();
            keyStore = keyStoreManager.load( keyStoreData );
            keyStoreData.setAlias( p.getId().toString() );

            keyStoreManager.importCertificateHEXString( keyStore, keyStoreData );
            //***********************************************************************

            //************ Send Trust SSL Cert **************************************
            KeyStore myKeyStore;
            KeyStoreData myKeyStoreData;
            KeyStoreManager myKeyStoreManager;

            myKeyStoreData = new KeyStoreData();
            myKeyStoreData.setupKeyStorePx2();

            myKeyStoreManager = new KeyStoreManager();
            myKeyStore = myKeyStoreManager.load( myKeyStoreData );

            String hexCert = myKeyStoreManager.exportCertificateHEXString( myKeyStore, myKeyStoreData );
            //***********************************************************************

            //sslContextFactory.reloadTrustStore();

            return Response.ok( hexCert ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving registration request #approveForRegistrationRequest", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response approveForRegistrationRequest( final String peerId )
    {
        try
        {
            Preconditions.checkState( UUIDUtil.isStringAUuid( peerId ) );

            UUID uuid = UUID.fromString( peerId );
            PeerInfo remotePeer = peerManager.getPeerInfo( uuid );
            PeerInfo peerToUpdateOnRemote = peerManager.getLocalPeerInfo();

            if ( remotePeer.getStatus() != PeerStatus.REQUESTED )
            {
                return Response.serverError().entity( "*********** Access denied *************" ).build();
            }
            else
            {
                //************ Send Trust SSL Cert **************************************
                KeyStore keyStore;
                KeyStoreData keyStoreData;
                KeyStoreManager keyStoreManager;

                keyStoreData = new KeyStoreData();
                keyStoreData.setupKeyStorePx2();

                keyStoreManager = new KeyStoreManager();
                keyStore = keyStoreManager.load( keyStoreData );

                String cert = keyStoreManager.exportCertificateHEXString( keyStore, keyStoreData );
                //***********************************************************************

                String baseUrl =
                        String.format( "https://%s:%s/cxf", remotePeer.getIp(), ChannelSettings.SECURE_PORT_X1 );
                WebClient client = restUtil.getTrustedWebClient( baseUrl );
                client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

                Form form = new Form();
                form.set( "approvedPeer", jsonUtil.to( peerToUpdateOnRemote ) );
                form.set( "root_cert_px2", cert );


                Response response = client.path( "peer/approve" ).put( form );
                if ( response.getStatus() == Response.Status.OK.getStatusCode() )
                {
                    LOGGER.info( response.readEntity( String.class ) );
                    remotePeer.setStatus( PeerStatus.APPROVED );
                    String root_cert_px2 = response.readEntity( String.class );
                    //************ Save Trust SSL Cert **************************************

                    keyStoreData = new KeyStoreData();
                    keyStoreData.setupTrustStorePx2();
                    keyStoreData.setHEXCert( root_cert_px2 );
                    keyStoreData.setAlias( remotePeer.getId().toString() );

                    keyStoreManager = new KeyStoreManager();
                    keyStore = keyStoreManager.load( keyStoreData );

                    keyStoreManager.importCertificateHEXString( keyStore, keyStoreData );
                    //***********************************************************************

                    //sslContextFactory.reloadTrustStore();

                    remotePeer.setStatus( PeerStatus.APPROVED );

                    peerManager.update( remotePeer );
                    return Response.ok( String.format( "%s registered.", remotePeer.getName() ) ).build();
                }
                else
                {
                    LOGGER.warn( "Response for registering peer: " + response.toString() );
                    return Response.status( Response.Status.EXPECTATION_FAILED )
                                   .entity( "Error occurred on peer to register" ).build();
                }
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error approving peer registration request #approveForRegistrationRequest", e );
            return Response.serverError().entity( e.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( String peer, String rootCertPx2 )
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


    @Override
    public Response destroyContainer( final String containerId )
    {
        try
        {
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).dispose();
            }

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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).start();
            }
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( containerId );
            if ( host instanceof ContainerHost )
            {
                ( ( ContainerHost ) host ).stop();
            }
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            ContainerHostState containerHostState =
                    localPeer.getContainerHostById( UUID.fromString( containerId ) ).getState();
            return Response.ok( jsonUtil.to( containerHostState ) ).build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response
                    .ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getAvailableRamQuota() )
                    .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response
                    .ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getAvailableCpuQuota() )
                    .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                      .getAvailableDiskQuota(
                                                              jsonUtil.<DiskPartition>from( diskPartition,
                                                                      new TypeToken<DiskPartition>()
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            ProcessResourceUsage processResourceUsage = localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                                 .getProcessResourceUsage( processPid );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getRamQuota() )
                           .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getRamQuotaInfo() )
                           .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setRamQuota( ram );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuQuota() )
                           .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuQuotaInfo() )
                           .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setCpuQuota( cpu );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response
                    .ok( jsonUtil.to( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuSet() ) )
                    .build();
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( jsonUtil.to( localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                      .getDiskQuota( JsonUtil.<DiskPartition>fromJson( diskPartition,
                                                              new TypeToken<DiskPartition>()
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setDefaultGateway( gatewayIp );
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
            Preconditions.checkState( UUIDUtil.isStringAUuid( containerId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();

            UUID uuid = UUID.fromString( containerId );

            return Response.ok( jsonUtil.to( localPeer.getContainerHostInfoById( uuid ) ) ).build();
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
    public Response reserveVni( final String vni )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            int vlan = localPeer.reserveVni( jsonUtil.from( vni, Vni.class ) );
            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error reserving vni #reserveVni", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response importEnvironmentCert( final String envCert, final String alias )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.importCertificate( envCert, alias );
            return Response.noContent().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error importing environment certificate #importEnvironmentCert", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response exportEnvironmentCert( final String environmentId )
    {
        try
        {
            Preconditions.checkState( UUIDUtil.isStringAUuid( environmentId ) );

            LocalPeer localPeer = peerManager.getLocalPeer();
            String certHEX = localPeer.exportEnvironmentCertificate( UUID.fromString( environmentId ) );
            return Response.ok( certHEX ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error exporting environment certificate #exportEnvironmentCert", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response removeEnvironmentCert( final String environmentId )
    {
        try
        {
            Preconditions.checkState( UUIDUtil.isStringAUuid( environmentId ) );

            UUID environmentUUID = UUID.fromString( environmentId );
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.removeEnvironmentCertificates( environmentUUID );
            return Response.noContent().build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error removing environment certificate #removeEnvironmentCert", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage() ).build();
        }
    }
}