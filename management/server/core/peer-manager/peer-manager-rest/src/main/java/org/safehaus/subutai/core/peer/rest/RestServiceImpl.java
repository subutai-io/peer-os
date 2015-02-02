package org.safehaus.subutai.core.peer.rest;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.host.ContainerHostState;
import org.safehaus.subutai.common.metric.ProcessResourceUsage;
import org.safehaus.subutai.common.peer.ContainerHost;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.HostInfoModel;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.protocol.Criteria;
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;


public class RestServiceImpl implements RestService
{

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private PeerManager peerManager;


    public RestServiceImpl( final PeerManager peerManager )
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
    public Response setQuota( final String hostId, final String quotaInfo )
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
    public Response getQuota( final String hostId, final String quotaType )
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

        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            Set<HostInfoModel> result = localPeer.scheduleCloneContainers( UUID.fromString( creatorPeerId ),
                    JsonUtil.<List<Template>>fromJson( templates, new TypeToken<List<Template>>()
                    {}.getType() ), quantity, strategyId,
                    JsonUtil.<List<Criteria>>fromJson( templates, new TypeToken<List<Criteria>>()
                    {}.getType() ) );
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
    public Response getContainerState( final String containerId )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            ContainerHostState containerHostState = localPeer.getContainerHostState( containerId );
            return Response.ok( JsonUtil.toJson( containerHostState ) ).build();
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


    //*********** Quota functions ***************


    @Override
    public Response getProcessResourceUsage( final String hostId, final int processPid )
    {
        try
        {
            LocalPeer localPeer = peerManager.getLocalPeer();
            ProcessResourceUsage processResourceUsage =
                    localPeer.getProcessResourceUsage( UUID.fromString( hostId ), processPid );
            return Response.ok( GSON.toJson( processResourceUsage ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getRamQuota( final String containerId )
    {
        try
        {
            return Response.ok( peerManager.getLocalPeer().getRamQuota( UUID.fromString( containerId ) ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setRamQuota( final String containerId, final int ram )
    {
        try
        {
            peerManager.getLocalPeer().setRamQuota( UUID.fromString( containerId ), ram );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuQuota( final String containerId )
    {
        try
        {
            return Response.ok( peerManager.getLocalPeer().getCpuQuota( UUID.fromString( containerId ) ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuQuota( final String containerId, final int cpu )
    {
        try
        {
            peerManager.getLocalPeer().setCpuQuota( UUID.fromString( containerId ), cpu );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getCpuSet( final String containerId )
    {
        try
        {
            return Response
                    .ok( JsonUtil.toJson( peerManager.getLocalPeer().getCpuSet( UUID.fromString( containerId ) ) ) )
                    .build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setCpuSet( final String containerId, final String cpuSet )
    {
        try
        {
            peerManager.getLocalPeer().setCpuSet( UUID.fromString( containerId ),
                    JsonUtil.<Set<Integer>>fromJson( cpuSet, new TypeToken<Set<Integer>>()
                    {}.getType() ) );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response getDiskQuota( final String containerId, final String diskPartition )
    {
        try
        {
            return Response.ok( JsonUtil.toJson( peerManager.getLocalPeer()
                                                            .getDiskQuota( UUID.fromString( containerId ),
                                                                    JsonUtil.<DiskPartition>from( diskPartition,
                                                                            new TypeToken<DiskPartition>()
                                                                            {}.getType() ) ) ) ).build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response setDiskQuota( final String containerId, final String diskQuota )
    {
        try
        {
            peerManager.getLocalPeer().setDiskQuota( UUID.fromString( containerId ),
                    JsonUtil.<DiskQuota>fromJson( diskQuota, new TypeToken<DiskQuota>()
                    {}.getType() ) );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }
}