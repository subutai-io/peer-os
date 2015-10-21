package io.subutai.core.peer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.PeerException;
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
    public RegistrationData processRegistrationRequest( final RegistrationData registrationData )
    {
        try
        {
            return peerManager.processRegistrationRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processCancelRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processCancelRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processRejectRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processRejectRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public RegistrationData processApproveRequest( final RegistrationData registrationData )
    {
        try
        {
            return peerManager.processApproveRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void processUnregisterRequest( final RegistrationData registrationData )
    {
        try
        {
            peerManager.processUnregisterRequest( registrationData );
        }
        catch ( PeerException e )
        {
            LOG.error( e.getMessage(), e );
            Response response = Response.serverError().build();
            throw new WebApplicationException( response );
        }
    }
}
