package io.subutai.core.environment.metadata.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.bazaar.share.event.payload.Payload;
import io.subutai.common.host.SubutaiOrigin;
import io.subutai.core.environment.metadata.api.EnvironmentMetadataManager;
import io.subutai.core.identity.api.exception.TokenCreateException;


public class RestServiceImpl implements RestService
{
    private static Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );

    private EnvironmentMetadataManager environmentMetadataManager;


    public RestServiceImpl( EnvironmentMetadataManager environmentMetadataManager )
    {
        this.environmentMetadataManager = environmentMetadataManager;
    }


    @Override
    public Response issueToken( String containerIp )
    {
        try
        {
            environmentMetadataManager.issueToken( containerIp );
            LOG.debug( "Token successfully generated." );
            return Response.noContent().build();
        }
        catch ( TokenCreateException e )
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response echo( final String containerId, String message )
    {
        return Response.ok( String.format( "You are %s and your message is %s.", containerId, message ) ).build();
    }


    @Override
    public Response getEnvironmentDto( final SubutaiOrigin origin, final String type )
    {
        Payload environmentInfoDto = environmentMetadataManager.getEnvironmentInfoDto( origin.getEnvironmentId(), type );
        return Response.ok( environmentInfoDto ).build();
    }


    @Override
    public Response pushEvent( final SubutaiOrigin origin, final Payload payload )
    {
        environmentMetadataManager.pushEvent( payload );
        return Response.noContent().build();
    }
}
