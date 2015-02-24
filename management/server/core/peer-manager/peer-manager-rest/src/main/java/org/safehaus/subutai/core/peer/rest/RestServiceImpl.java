package org.safehaus.subutai.core.peer.rest;


import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.common.quota.DiskPartition;
import org.safehaus.subutai.common.quota.DiskQuota;
import org.safehaus.subutai.common.quota.PeerQuotaInfo;
import org.safehaus.subutai.common.quota.QuotaInfo;
import org.safehaus.subutai.common.quota.QuotaType;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import com.google.gson.reflect.TypeToken;


public class RestServiceImpl implements RestService
{

    private PeerManager peerManager;


    public RestServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public Response getSelfPeerInfo()
    {
        PeerInfo selfInfo = peerManager.getLocalPeerInfo();
        selfInfo.setIp( getRequestIp() );
        selfInfo.setName( String.format( "Peer on %s", selfInfo.getIp() ) );
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
    public Response getRegisteredPeerInfo( @QueryParam( "peerId" ) final String peerId )
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
    public Response processRegisterRequest( String peer , String root_cert_px1)
    {
        PeerInfo p = JsonUtil.fromJson( peer, PeerInfo.class );
        p.setIp( getRequestIp() );
        p.setName( String.format( "Peer on %s", p.getIp() ) );
        try
        {
            peerManager.register( p );
            return Response.ok( JsonUtil.toJson( p ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.NOT_FOUND ).entity( e.toString() ).build();
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
    public Response updatePeer( String peer , String root_cert_px1 )
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
                                                                          {
                                                                          }.getType() ) ) ) ).build();
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
                     {
                     }.getType() ) );
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
                                                                  {
                                                                  }.getType() ) ) ) ).build();
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
                     {
                     }.getType() ) );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getTakenVni()
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            return Response.ok( JsonUtil.toJson( localPeer.getTakenVniIds() ) ).build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setupTunnels( final Set<String> peerIps, final long vni, final boolean newVni )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            localPeer.setupTunnels( peerIps, vni, newVni );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}