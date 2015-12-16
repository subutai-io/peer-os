package io.subutai.core.peer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.peer.RegistrationData;
import io.subutai.core.peer.api.PeerManager;


/**
 * REST endpoint implementation of registration process
 */
public class RegistrationServiceImpl implements RegistrationService
{
    private static Logger LOG = LoggerFactory.getLogger( RegistrationData.class );

    private PeerManager peerManager;


    public RegistrationServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public Response getPeerInfo()
    {
        PeerInfo r = peerManager.getLocalPeer().getPeerInfo();

        if ( r == null )
        {
            return Response.serverError().entity( "Peer info not available." ).build();
        }
        else
        {
            return Response.ok( r ).build();
        }
    }


    @Override
    public Response processRegistrationRequest( final RegistrationData registrationData )
    {
        try
        {
            RegistrationData r = peerManager.processRegistrationRequest( registrationData );
            return Response.ok( r ).build();
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response processCancelRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processCancelRequest( registrationData );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().entity( e.getMessage() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public Response processRejectRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processRejectRequest( registrationData );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response processApproveRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processApproveRequest( registrationData );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }


    @Override
    public Response processUnregisterRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processUnregisterRequest( registrationData );
            return Response.ok().build();
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            return Response.serverError().entity( e.getMessage() ).build();
        }
    }
}
