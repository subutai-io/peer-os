package io.subutai.core.peer.rest.ui;


import java.security.AccessControlException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.common.settings.Common;
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


    @RolesAllowed( { "Peer-Management|Read" } )
    @Override
    public Response getRegisteredPeers()
    {
        try
        {
            List<PeerDto> registrationDatas =
                    peerManager.getRegistrationRequests().stream().map( PeerDto::new ).collect( Collectors.toList() );

            return Response.ok( JsonUtil.toJson( registrationDatas ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting registered peers #getRegisteredPeers", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @RolesAllowed( { "Peer-Management|Read" } )
    @Override
    public Response getRegisteredPeersStates()
    {
        try
        {
            List<PeerDto> registrationDatas =
                    peerManager.getRegistrationRequests().stream().map( PeerDto::new ).collect( Collectors.toList() );

            if ( !registrationDatas.isEmpty() )
            {
                ExecutorService taskExecutor =
                        Executors.newFixedThreadPool( Math.min( Common.MAX_EXECUTOR_SIZE, registrationDatas.size() ) );

                List<CompletableFuture> futures =
                        registrationDatas.stream().map( d -> CompletableFuture.runAsync( () -> {

                            if ( d.getRegistrationData().getStatus() == RegistrationStatus.APPROVED )
                            {
                                try
                                {
                                    d.setState( peerManager.getPeer( d.getRegistrationData().getPeerInfo().getId() )
                                                           .isOnline() ? PeerDto.State.ONLINE : PeerDto.State.OFFLINE );
                                }
                                catch ( PeerException e )
                                {
                                    LOGGER.error( "Exceptions getting peer status", e );
                                }
                            }
                        }, taskExecutor ) ).collect( Collectors.toList() );

                CompletableFuture.allOf( futures.toArray( new CompletableFuture[0] ) ).join();
            }

            return Response.ok( JsonUtil.toJson( registrationDatas ) ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error getting registered peers #getRegisteredPeersStates", e );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).entity( e.toString() ).build();
        }
    }


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
    @Override
    public Response processRegisterRequest( final String ip, final String keyPhrase )
    {
        try
        {
            peerManager.doRegistrationRequest( ip, keyPhrase );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public Response rejectForRegistrationRequest( final String peerId, Boolean force )
    {
        try
        {
            peerManager.doRejectRequest( peerId, force );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Write", "Peer-Management|Update" } )
    @Override
    public Response approveForRegistrationRequest( final String peerId, final String keyPhrase )
    {
        try
        {
            peerManager.doApproveRequest( keyPhrase, peerId );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public Response cancelForRegistrationRequest( final String peerId, Boolean force )
    {
        try
        {
            peerManager.doCancelRequest( peerId, force );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Update" } )
    @Override
    public Response renamePeer( final String peerId, final String name )
    {
        try
        {
            peerManager.setName( peerId, name );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Update" } )
    @Override
    public Response updatePeerUrl( final String peerId, final String ip )
    {
        try
        {
            peerManager.updatePeerUrl( peerId, ip );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Delete", "Peer-Management|Update" } )
    @Override
    public Response unregisterForRegistrationRequest( final String peerId, Boolean force )
    {
        try
        {
            peerManager.doUnregisterRequest( peerId, force );
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOGGER.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

            return Response.status( Response.Status.BAD_REQUEST ).entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }


    @RolesAllowed( { "Peer-Management|Read" } )
    @Override
    public Response getResourceHosts()
    {
        return Response.ok().entity( JsonUtil.toJson( hostRegistry.getResourceHostsInfo() ) ).build();
    }


    @RolesAllowed( { "Peer-Management|Read" } )
    @Override
    public Response checkPeer( String destinationHost )
    {
        try
        {
            peerManager.checkHostAvailability( destinationHost );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( e.getMessage() ).build();
        }

        return Response.ok().build();
    }
}
