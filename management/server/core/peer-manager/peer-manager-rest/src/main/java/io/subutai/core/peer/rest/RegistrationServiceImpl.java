package io.subutai.core.peer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RegistrationRequest;
import io.subutai.core.peer.api.PeerManager;


/**
 * REST endpoint implementation of registration process
 */
public class RegistrationServiceImpl implements RegistrationService
{
    private static Logger LOG = LoggerFactory.getLogger( RegistrationRequest.class );

    private PeerManager peerManager;


    public RegistrationServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public RegistrationRequest processRegistrationRequest( final RegistrationRequest registrationRequest )
    {
        try
        {
            return peerManager.processRegistrationRequest( registrationRequest );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processCancelRequest( final RegistrationRequest registrationRequest )
    {
        try
        {
            peerManager.processCancelRequest( registrationRequest );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processRejectRequest( final RegistrationRequest registrationRequest )
    {
        try
        {
            peerManager.processRejectRequest( registrationRequest );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public RegistrationRequest processApproveRequest( final RegistrationRequest registrationRequest )
    {
        try
        {
            return peerManager.processApproveRequest( registrationRequest );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processUnregisterRequest( final RegistrationRequest registrationRequest )
    {
        try
        {
            peerManager.processUnregisterRequest( registrationRequest );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }
}
