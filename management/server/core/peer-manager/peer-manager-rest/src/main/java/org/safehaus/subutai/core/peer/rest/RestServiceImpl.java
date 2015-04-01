package org.safehaus.subutai.core.peer.rest;


import java.security.KeyStore;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.network.Vni;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.peer.PeerPolicy;
import org.safehaus.subutai.common.peer.PeerStatus;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.quota.RamQuota;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.safehaus.subutai.common.settings.ChannelSettings;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.gson.reflect.TypeToken;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private PeerManager peerManager;
    private CustomSslContextFactory sslContextFactory;


    public RestServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setSslContextFactory( final CustomSslContextFactory sslContextFactory )
    {
        this.sslContextFactory = sslContextFactory;
    }


    @Override
    public Response getSelfPeerInfo()
    {
        PeerInfo selfInfo = peerManager.getLocalPeerInfo();
        return Response.ok( JsonUtil.toJson( selfInfo ) ).build();
    }


    @Override
    public String getId()
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        return localPeer.getId().toString();
    }


    @Override
    public Response getRegisteredPeers()
    {
        return Response.ok( JsonUtil.toJson( peerManager.peers() ) ).build();
    }


    @Override
    public Response getPeerPolicy( final String peerId )
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
            return Response.ok( JsonUtil.toJson( JsonUtil.toJson( peerPolicy ) ) ).build();
        }
    }


    @Override
    public Response getRegisteredPeerInfo( final String peerId )
    {
        PeerInfo peerInfo = peerManager.getPeer( peerId ).getPeerInfo();
        return Response.ok( JsonUtil.toJson( peerInfo ) ).build();
    }


    @Override
    public Response ping()
    {
        return Response.ok().build();
    }


    @Override
    public Response processTrustRequest( String peer, String root_cert_px2 )
    {
        try
        {
            return null;
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response processTrustResponse( String peer, String root_cert_px2, short status )
    {
        try
        {
            return null;
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response processRegisterRequest( String peer )
    {
        PeerInfo p = JsonUtil.fromJson( peer, PeerInfo.class );
        LOGGER.debug( peer );

        if ( peerManager.getPeerInfo( p.getId() ) != null )
        {
            return Response.status( Response.Status.CONFLICT )
                           .entity( String.format( "%s already registered", p.getName() ) ).build();
        }

        p.setStatus( PeerStatus.REQUESTED );
        p.setName( String.format( "Peer on %s", p.getIp() ) );
        try
        {
            peerManager.register( p );
            return Response.ok( JsonUtil.toJson( peerManager.getLocalPeerInfo() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response unregisterPeer( String peerId )
    {
        UUID id = JsonUtil.fromJson( peerId, UUID.class );
        try
        {
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

                //                new Thread( new RestartCoreServlet() ).start();
                sslContextFactory.reloadTrustStore();

                return Response.ok( "Successfully unregistered peer: " + peerId ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( Exception pe )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( pe.toString() ).build();
        }
    }


    @Override
    public Response rejectForRegistrationRequest( final String rejectedPeerId )
    {
        PeerInfo p = peerManager.getPeerInfo( UUID.fromString( rejectedPeerId ) );
        p.setStatus( PeerStatus.REJECTED );
        peerManager.update( p );

        return Response.noContent().build();
    }


    @Override
    public Response removeRegistrationRequest( final String rejectedPeerId )
    {
        try
        {
            UUID id = JsonUtil.fromJson( rejectedPeerId, UUID.class );
            peerManager.unregister( id.toString() );
            return Response.status( Response.Status.NO_CONTENT ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response approveForRegistrationRequest( final String approvedPeer, final String root_cert_px2 )
    {
        PeerInfo p = JsonUtil.fromJson( approvedPeer, PeerInfo.class );
        p.setStatus( PeerStatus.APPROVED );
        peerManager.update( p );

        //************ Save Trust SSL Cert **************************************
        KeyStore keyStore;
        KeyStoreData keyStoreData;
        KeyStoreManager keyStoreManager;

        keyStoreData = new KeyStoreData();
        keyStoreData.setupTrustStorePx2();
        keyStoreData.setHEXCert( root_cert_px2 );
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

        String HEXCert = myKeyStoreManager.exportCertificateHEXString( myKeyStore, myKeyStoreData );


        //***********************************************************************

        sslContextFactory.reloadTrustStore();
        //        new Thread( new RestartCoreServlet() ).start();


        return Response.ok( HEXCert ).build();
    }


    @Override
    public Response approveForRegistrationRequest( final String peerId )
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

        UUID uuid = JsonUtil.fromJson( peerId, UUID.class );
        PeerInfo remotePeer = peerManager.getPeerInfo( uuid );
        PeerInfo peerToUpdateOnRemote = peerManager.getLocalPeerInfo();

        String baseUrl = String.format( "https://%s:%s/cxf", remotePeer.getIp(), ChannelSettings.SECURE_PORT_X1 );
        WebClient client = RestUtil.createTrustedWebClient( baseUrl );//WebClient.create( baseUrl );
        client.type( MediaType.APPLICATION_FORM_URLENCODED ).accept( MediaType.APPLICATION_JSON );

        Form form = new Form();
        form.set( "approvedPeer", JsonUtil.toJson( peerToUpdateOnRemote ) );
        form.set( "root_cert_px2", cert );

        try
        {
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

                sslContextFactory.reloadTrustStore();

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
        catch ( Exception e )
        {
            return Response.serverError().entity( e.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( String peer, String root_cert_px1 )
    {
        PeerInfo p = JsonUtil.fromJson( peer, PeerInfo.class );
        p.setIp( getRequestIp() );
        p.setName( String.format( "Peer on %s", p.getIp() ) );
        peerManager.update( p );

        return Response.ok( JsonUtil.toJson( p ) ).build();
    }


    @Override
    public Response setQuota( final String containerId, final String quotaInfo )
    {
        try
        {
            QuotaInfo q = JsonUtil.fromJson( quotaInfo, QuotaInfo.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setQuota( q );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getQuota( final String containerId, final String quotaType )
    {
        try
        {
            QuotaType q = JsonUtil.fromJson( quotaType, QuotaType.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            PeerQuotaInfo quotaInfo = localPeer.getContainerHostById( UUID.fromString( containerId ) ).getQuota( q );
            return Response.ok( JsonUtil.toJson( quotaInfo ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getQuotaInfo( final String containerId, final String quotaType )
    {
        try
        {
            QuotaType q = JsonUtil.fromJson( quotaType, QuotaType.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            QuotaInfo quotaInfo = localPeer.getContainerHostById( UUID.fromString( containerId ) ).getQuotaInfo( q );
            return Response.ok( JsonUtil.toJson( quotaInfo ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    private String getRequestIp()
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
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response startContainer( final String containerId )
    {
        try
        {
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
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response stopContainer( final String containerId )
    {
        try
        {
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
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response isContainerConnected( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Boolean result = localPeer.bindHost( containerId ).isConnected();
            return Response.ok( result.toString() ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getContainerState( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            ContainerHostState containerHostState =
                    localPeer.getContainerHostById( UUID.fromString( containerId ) ).getState();
            return Response.ok( JsonUtil.toJson( containerHostState ) ).build();
        }
        catch ( Exception e )
        {
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
            return Response.ok( JsonUtil.toJson( result ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    //*********** Quota functions ***************


    @Override
    public Response getAvailableRamQuota( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response
                    .ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getAvailableRamQuota() )
                    .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableCpuQuota( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response
                    .ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getAvailableCpuQuota() )
                    .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getAvailableDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson( localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                          .getAvailableDiskQuota(
                                                                  JsonUtil.<DiskPartition>from( diskPartition,
                                                                          new TypeToken<DiskPartition>()
                                                                          {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getProcessResourceUsage( final String containerId, final int processPid )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            ProcessResourceUsage processResourceUsage = localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                                 .getProcessResourceUsage( processPid );
            return Response.ok( JsonUtil.toJson( processResourceUsage ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getRamQuota() )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuotaInfo( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getRamQuotaInfo() )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setRamQuota( ram );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuQuota() )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuotaInfo( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuQuotaInfo() )
                           .build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) ).setCpuQuota( cpu );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuSet( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil
                    .toJson( localPeer.getContainerHostById( UUID.fromString( containerId ) ).getCpuSet() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final String containerId, final String cpuSet )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
                     .setCpuSet( JsonUtil.<Set<Integer>>fromJson( cpuSet, new TypeToken<Set<Integer>>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson( localPeer.getContainerHostById( UUID.fromString( containerId ) )
                                                          .getDiskQuota( JsonUtil.<DiskPartition>from( diskPartition,
                                                                  new TypeToken<DiskPartition>()
                                                                  {}.getType() ) ) ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final String ramQuota )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
                     .setRamQuota( JsonUtil.<RamQuota>fromJson( ramQuota, new TypeToken<RamQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.getContainerHostById( UUID.fromString( containerId ) )
                     .setDiskQuota( JsonUtil.<DiskQuota>fromJson( diskQuota, new TypeToken<DiskQuota>()
                     {}.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDefaultGateway( final String containerId, final String gatewayIp )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.setDefaultGateway( localPeer.getContainerHostById( UUID.fromString( containerId ) ), gatewayIp );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getContainerHostInfoById( final String containerId )
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        try
        {
            UUID uuid = JsonUtil.fromJson( containerId, UUID.class );

            return Response.ok( JsonUtil.toJson( localPeer.getContainerHostInfoById( uuid ) ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getReservedVnis()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson( localPeer.getReservedVnis() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getGateways()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson( localPeer.getGateways() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response reserveVni( final String vni )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            int vlan = localPeer.reserveVni( JsonUtil.fromJson( vni, Vni.class ) );
            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
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
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response exportEnvironmentCert( final String environmentId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            String certHEX = localPeer.exportEnvironmentCertificate( UUID.fromString( environmentId ) );
            return Response.ok( certHEX ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response removeEnvironmentCert( final String environmentId )
    {
        try
        {
            UUID environmentUUID = JsonUtil.fromJson( environmentId, UUID.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.removeEnvironmentCertificates( environmentUUID );
            return Response.noContent().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.getMessage() ).build();
        }
    }
}