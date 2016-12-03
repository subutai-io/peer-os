package io.subutai.localpeer.rest.ui;


import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.subutai.common.host.ContainerHostInfo;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.JsonUtil;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class );
    private final LocalPeer localPeer;


    public RestServiceImpl( final LocalPeer localPeer )
    {
        Preconditions.checkNotNull( localPeer );

        this.localPeer = localPeer;
    }


    @Override
    public Response getNotRegisteredContainers()
    {
        return Response.ok( JsonUtil.toJson( localPeer.getNotRegisteredContainers() ) ).build();
    }


    @Override
    public Response destroyNotRegisteredContainer( final String containerId )
    {
        try
        {
            boolean notRegistered = false;

            for ( ContainerHostInfo containerHostInfo : localPeer.getNotRegisteredContainers() )
            {
                if ( containerHostInfo.getId().equals( containerId ) )
                {
                    notRegistered = true;

                    break;
                }
            }

            if ( notRegistered )
            {
                localPeer.destroyContainer( new ContainerId( containerId ) );

                return Response.ok().build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage(), e );
            throw new WebApplicationException( Response.serverError().entity( e.getMessage() ).build() );
        }
    }
}
