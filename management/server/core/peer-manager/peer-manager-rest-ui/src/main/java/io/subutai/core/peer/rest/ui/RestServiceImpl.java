package io.subutai.core.peer.rest.ui;


import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.RegistrationData;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.hostregistry.api.HostRegistry;
import io.subutai.core.peer.api.PeerManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );

    private PeerManager peerManager;
    private HostRegistry hostRegistry;


    public RestServiceImpl( final PeerManager peerManager, final HostRegistry hostRegistry )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( hostRegistry );

        this.peerManager = peerManager;
        this.hostRegistry = hostRegistry;
    }


    @Override
    public Response getRegisteredPeers()
    {
        try
        {
            return Response.ok( JsonUtil.toJson( peerManager.getRegistrationRequests() ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting registered peers #getRegisteredPeers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @Override
    public Response processRegisterRequest( final String ip, final String keyPhrase )
    {
        try
        {
            peerManager.doRegistrationRequest( ip, keyPhrase );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response rejectForRegistrationRequest( final String peerId )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doRejectRequest( data );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response approveForRegistrationRequest( final String peerId, final String keyPhrase )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doApproveRequest( keyPhrase, data );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response cancelForRegistrationRequest( final String peerId )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doCancelRequest( data );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response unregisterForRegistrationRequest( final String peerId )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doUnregisterRequest( data );
        }
        catch ( Exception e )
        {
            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @Override
    public Response getResourceHosts()
    {
        return Response.ok().entity( JsonUtil.toJson( hostRegistry.getResourceHostsInfo() ) ).build();
    }
}
