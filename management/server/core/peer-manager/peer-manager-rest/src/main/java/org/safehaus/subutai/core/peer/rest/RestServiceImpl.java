package org.safehaus.subutai.core.peer.rest;


import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostInfoModel;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;


/**
 * Created by bahadyr on 5/6/14.
 */

public class RestServiceImpl implements RestService
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private PeerManager peerManager;


    public RestServiceImpl()
    {
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public String getId()
    {
        LocalPeer localPeer = peerManager.getLocalPeer();
        return localPeer.getId().toString();
    }


    @Override
    public PeerInfo registerPeer( String config )
    {
        if ( config != null )
        {
            PeerInfo peerInfo = GSON.fromJson( config, PeerInfo.class );
            peerInfo.setIp( getRequestIp() );
            try
            {
                peerManager.register( peerInfo );
            }
            catch ( PeerException e )
            {
                return null;
            }
            return peerInfo;
        }
        else
        {
            return null;
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
        PeerInfo p = GSON.fromJson( peer, PeerInfo.class );

        p.setIp( getRequestIp() );
        try
        {
            peerManager.register( p );
            return Response.ok( GSON.toJson( p ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response unregisterPeer( String peerId )
    {
        UUID id = GSON.fromJson( peerId, UUID.class );
        try
        {
            boolean result = peerManager.unregister( id.toString() );
            if ( result )
            {
                return Response.ok( "Successfully unregistered peer: " + peerId ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( PeerException pe )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( pe.toString() ).build();
        }
    }


    @Override
    public Response updatePeer( String peer )
    {
        PeerInfo p = GSON.fromJson( peer, PeerInfo.class );
        p.setIp( getRequestIp() );
        peerManager.update( p );
        return Response.ok( GSON.toJson( p ) ).build();
    }


    @Override
    public Response setQuota( @FormParam( "hostId" ) final String hostId,
                              @FormParam( "quotaInfo" ) final String quotaInfo )
    {
        try
        {
            QuotaInfo q = GSON.fromJson( quotaInfo, QuotaInfo.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.setQuota( localPeer.getContainerHostById( hostId ), q );
            return Response.ok().build();
        }
        catch ( JsonParseException | PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getQuota( @QueryParam( "hostId" ) final String hostId,
                              @QueryParam( "quotaType" ) final String quotaType )
    {
        try
        {
            QuotaType q = GSON.fromJson( quotaType, QuotaType.class );
            LocalPeer localPeer = peerManager.getLocalPeer();
            PeerQuotaInfo quotaInfo = localPeer.getQuota( localPeer.getContainerHostById( hostId ), q );
            return Response.ok( GSON.toJson( quotaInfo ) ).build();
        }
        catch ( JsonParseException | PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    private static String getLocalIp()
    {
        Enumeration<NetworkInterface> n;
        try
        {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); )
            {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); )
                {
                    InetAddress addr = a.nextElement();
                    if ( !addr.getHostAddress().startsWith( "10" ) && addr.isSiteLocalAddress() )
                    {
                        return ( addr.getHostName() );
                    }
                }
            }
        }
        catch ( SocketException e )
        {
            System.out.println( e.getMessage() );
        }

        return "127.0.0.1";
    }


    private String getRequestIp()
    {
        Message message = PhaseInterceptorChain.getCurrentMessage();
        HttpServletRequest request = ( HttpServletRequest ) message.get( AbstractHTTPDestination.HTTP_REQUEST );
        return request.getRemoteAddr();
    }


    @Override
    public Response scheduleCloneContainers( final String creatorPeerId, final String templates, final int quantity,
                                             final String strategyId, final String criteria )
    {

        List<Criteria> criteriaList = new ArrayList();
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Set<HostInfoModel> result = localPeer.scheduleCloneContainers( UUID.fromString( creatorPeerId ),
                    ( List<Template> ) JsonUtil.fromJson( templates, new TypeToken<List<Template>>()
                    {}.getType() ), quantity, strategyId,
                    ( List<Criteria> ) JsonUtil.fromJson( templates, new TypeToken<List<Criteria>>()
                    {}.getType() ) );
            return Response.ok( JsonUtil.toJson( result ) ).build();
            //            return Response.ok().entity( result ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response environmentContainers( final String envId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            UUID environmentId = UUID.fromString( envId );

            Set<ContainerHost> result = localPeer.getContainerHostsByEnvironmentId( environmentId );
            return Response.ok( JsonUtil.toJson( result ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response destroyContainer( final String hostId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( hostId );
            if ( host instanceof ContainerHost )
            {
                localPeer.destroyContainer( ( ContainerHost ) host );
            }

            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response startContainer( final String hostId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( hostId );
            if ( host instanceof ContainerHost )
            {
                localPeer.startContainer( ( ContainerHost ) host );
            }
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response stopContainer( final String hostId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Host host = localPeer.bindHost( hostId );
            if ( host instanceof ContainerHost )
            {
                localPeer.stopContainer( ( ContainerHost ) host );
            }
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response isContainerConnected( final String hostId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Boolean result = localPeer.isConnected( localPeer.bindHost( hostId ) );
            return Response.ok( result.toString() ).build();
        }
        catch ( PeerException e )
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
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}