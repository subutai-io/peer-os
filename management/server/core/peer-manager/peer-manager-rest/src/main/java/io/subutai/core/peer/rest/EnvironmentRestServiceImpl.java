package io.subutai.core.peer.rest;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostState;
import io.subutai.common.peer.ContainerId;
import io.subutai.core.peer.api.PeerManager;


/**
 * Environment REST endpoint implementation
 */
public class EnvironmentRestServiceImpl implements EnvironmentRestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( EnvironmentRestServiceImpl.class );

    private PeerManager peerManager;


    public EnvironmentRestServiceImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    public void destroyContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.destroyContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error destroying container #destroyContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void startContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.startContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error starting container #startContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public void stopContainer( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        try
        {
            peerManager.stopContainer( containerId );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error stopping container #stopContainer", e );
            Response response = Response.serverError().entity( e.toString() ).build();
            throw new WebApplicationException( response );
        }
    }


    @Override
    public ContainerHostState getContainerState( final ContainerId containerId )
    {
        Preconditions.checkNotNull( containerId );
        return peerManager.getContainerState( containerId );
    }
}
