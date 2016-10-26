package io.subutai.core.peer.rest.ui;


import java.security.AccessControlException;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
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


    private class RegistrationDataDto
    {
        private boolean isOnline = false;
        private RegistrationData registrationData;


        public RegistrationDataDto( RegistrationData registrationData )
        {
            this.registrationData = registrationData;
        }


        public void setOnline( boolean isOnline )
        {
            this.isOnline = isOnline;
        }


        public RegistrationData getRegistrationData()
        {
            return registrationData;
        }
    }


    @Override
    public Response getRegisteredPeers()
    {
        try
        {
            List<RegistrationDataDto> registrationDatas =
                    peerManager.getRegistrationRequests().stream().map( d -> new RegistrationDataDto( d ) )
                               .collect( Collectors.toList() );

            if ( !registrationDatas.isEmpty() )
            {
                ExecutorService taskExecutor = Executors.newFixedThreadPool( registrationDatas.size() );

                CompletionService<Boolean> taskCompletionService = getCompletionService( taskExecutor );

                registrationDatas.forEach( d ->
                {
                    taskCompletionService.submit( () ->
                    {
                        try
                        {
                            if ( d.getRegistrationData().getStatus() == RegistrationStatus.APPROVED )
                            {
                                d.setOnline( peerManager.getPeer( d.getRegistrationData().getPeerInfo().getId() )
                                                        .isOnline() );
                            }
                        }
                        catch ( PeerException e )
                        {
                            LOGGER.error( "Exceptions getting peer status", e );
                        }

                        return true;
                    } );
                } );

                taskExecutor.shutdown();

                for ( int i = 0; i < registrationDatas.size(); i++ )
                {
                    try
                    {
                        Future<Boolean> future = taskCompletionService.take();
                        future.get();
                    }
                    catch ( ExecutionException | InterruptedException e )
                    {
                        LOGGER.error( e.getMessage() );
                    }
                }
            }

            return Response.ok( JsonUtil.toJson( registrationDatas ) ).build();
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


    @Override
    public Response rejectForRegistrationRequest( final String peerId, Boolean force )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doRejectRequest( data, force );
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


    @Override
    public Response cancelForRegistrationRequest( final String peerId, Boolean force )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doCancelRequest( data, force );
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


    @Override
    public Response unregisterForRegistrationRequest( final String peerId, Boolean force )
    {
        List<RegistrationData> dataList = peerManager.getRegistrationRequests();

        RegistrationData data =
                dataList.stream().filter( p -> p.getPeerInfo().getId().equals( peerId ) ).findAny().get();

        try
        {
            peerManager.doUnregisterRequest( data, force );
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


    @Override
    public Response getResourceHosts()
    {
        return Response.ok().entity( JsonUtil.toJson( hostRegistry.getResourceHostsInfo() ) ).build();
    }


    protected CompletionService<Boolean> getCompletionService( Executor executor )
    {
        return new ExecutorCompletionService<>( executor );
    }


    @Override
    public Response checkPeer( String destinationHost )
    {
        try
        {
            peerManager.checkHostAvailability( destinationHost );
        }
        catch ( Exception e )
        {
            return Response.serverError().entity( JsonUtil.toJson( e.getMessage() ) ).build();
        }

        return Response.ok().build();
    }
}
