package io.subutai.core.environment.metadata.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            return Response.status( Response.Status.UNAUTHORIZED ).build();
        }
    }


    @Override
    public Response echo( String message )
    {
        return Response.ok( message ).build();
    }
}
