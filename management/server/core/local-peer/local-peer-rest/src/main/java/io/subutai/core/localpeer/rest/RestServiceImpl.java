package io.subutai.core.localpeer.rest;


import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.bouncycastle.openpgp.PGPPublicKeyRing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.host.HostInterfaces;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.network.Gateway;
import io.subutai.common.network.Vni;
import io.subutai.common.peer.AlertPack;
import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.PeerPolicy;
import io.subutai.common.protocol.N2NConfig;
import io.subutai.common.protocol.Template;
import io.subutai.common.security.PublicKeyContainer;
import io.subutai.common.security.crypto.pgp.PGPKeyUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.protocol.TemplateKurjun;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private final LocalPeer localPeer;
    //    private Monitor monitor;
    protected JsonUtil jsonUtil = new JsonUtil();
    protected RestUtil restUtil = new RestUtil();


    public RestServiceImpl( final LocalPeer localPeer/*, Monitor monitor*/ )
    {
        this.localPeer = localPeer;
        //        this.monitor = monitor;
    }


    @Override
    public Response getLocalPeerInfo()
    {
        try
        {
            PeerInfo selfInfo = localPeer.getPeerInfo();
            return Response.ok( selfInfo ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating local peer info #getLocalPeerInfo", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return localPeer.getPeerInfo();
    }


    @Override
    public Response getPeerPolicy( final String peerId )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( peerId ) );

            PeerPolicy peerPolicy = localPeer.getPeerInfo().getPeerPolicy( peerId );
            if ( peerPolicy == null )
            {
                return Response.ok().build();
            }
            else
            {
                return Response.ok( peerPolicy ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting peer policy #getPeerPolicy", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( PeerInfo peerInfo )
    {
        try
        {
            //            PeerInfo p = jsonUtil.from( peerInfo, PeerInfo.class );
            //            p.setIp( getRequestIp() );
            //            p.setName( String.format( "Peer %s", p.getId() ) );
            //            localPeer.update( p );

            return Response.ok( /*jsonUtil.to( p )*/ ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error updating peerInfo #updatePeer", e );
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
    public Response getTemplate( final String templateName )
    {
        try
        {
            TemplateKurjun result = localPeer.getTemplate( templateName );
            return Response.ok( result ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting template #getTemplate", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDefaultGateway( final String containerId, final String gatewayIp )
    {
        try
        {
            Preconditions.checkArgument( !Strings.isNullOrEmpty( containerId ) );

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

            return Response.ok( jsonUtil.to( localPeer.getContainerHostInfoById( containerId ) ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting container host info by id #getContainerHostInfoById", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Collection<Vni> getReservedVnis()
    {
        try
        {
            return localPeer.getReservedVnis();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting reserved vnis #getReservedVnis", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Collection<Gateway> getGateways()
    {
        try
        {
            return localPeer.getGateways();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting gateways #getGateways", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response createGateway( final Gateway gateway )
    {
        try
        {
            localPeer.createGateway( gateway );

            return Response.ok().status( Response.Status.CREATED ).build();
        }
        catch ( Exception ex )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( ex ).build();
        }
    }


    @Override
    public PublicKeyContainer createEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        Preconditions.checkNotNull( environmentId );

        try
        {
            return localPeer.createPeerEnvironmentKeyPair( environmentId );
        }
        catch ( Exception ex )
        {
            throw new WebApplicationException( ex );
        }
    }


    @Override
    public void updateEnvironmentKey( final PublicKeyContainer publicKeyContainer )
    {
        Preconditions.checkNotNull( publicKeyContainer );
        Preconditions.checkNotNull( publicKeyContainer.getKey() );
        Preconditions.checkNotNull( publicKeyContainer.getHostId() );
        Preconditions.checkNotNull( publicKeyContainer.getFingerprint() );

        try
        {
            final PGPPublicKeyRing pubKeyRing = PGPKeyUtil.readPublicKeyRing( publicKeyContainer.getKey() );
            localPeer.updatePeerEnvironmentPubKey( new EnvironmentId( publicKeyContainer.getHostId() ), pubKeyRing );
        }
        catch ( Exception ex )
        {
            throw new WebApplicationException( ex );
        }
    }


    @Override
    public void removeEnvironmentKeyPair( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.removePeerEnvironmentKeyPair( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Vni reserveVni( final Vni vni )
    {
        try
        {
            return localPeer.reserveVni( vni );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error reserving vni #reserveVni", e );
            throw new WebApplicationException( e );
        }
    }


    @Override
    public Response setupTunnels( final String peerIps, final String environmentId )
    {
        try
        {
            int vlan = localPeer
                    .setupTunnels( jsonUtil.<Map<String, String>>from( peerIps, new TypeToken<Map<String, String>>()
                    {}.getType() ), environmentId );

            return Response.ok( vlan ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error setting up tunnels #setupTunnels", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public HostInterfaces getNetworkInterfaces()
    {
        try
        {
            return localPeer.getInterfaces();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public ResourceHostMetrics getResources()
    {
        try
        {
            return localPeer.getResourceHostMetrics();
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void setupN2NConnection( final N2NConfig config )
    {
        try
        {
            localPeer.setupN2NConnection( config );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void removeN2NConnection( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.removeN2NConnection( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void cleanupNetwork( final EnvironmentId environmentId )
    {
        try
        {
            localPeer.cleanupEnvironmentNetworkSettings( environmentId );
        }
        catch ( Exception e )
        {
            throw new WebApplicationException( e );
        }
    }


    @Override
    public void putAlert( final AlertPack alertPack )
    {
        try
        {
            localPeer.putAlert( alertPack );
        }
        catch ( PeerException e )
        {
            throw new WebApplicationException( e );
        }
    }
}
