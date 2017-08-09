package io.subutai.core.bazaar.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.bazaar.api.Bazaar;
import io.subutai.core.identity.api.IdentityManager;


public class RestServiceImpl implements RestService
{
    private Bazaar bazaar;
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class );
    private IdentityManager identityManager = ServiceLocator.lookup( IdentityManager.class );


    @Override
    public Response listProducts()
    {
        return Response.status( Response.Status.OK ).entity( bazaar.getProducts() ).build();
    }


    @Override
    public Response listInstalled()
    {
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( bazaar.getPlugins() ) ).build();
    }


    @Override
    public Response installPlugin( String name, String version, String kar, String url, String uid )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        try
        {
            bazaar.installPlugin( name, version, kar, url, uid );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response uninstallPlugin( Long id, String name )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        bazaar.uninstallPlugin( id, name );
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response restorePlugin( Long id, String name, String version, String kar, String url, String uid )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        try
        {
            bazaar.restorePlugin( id, name, version, kar, url, uid );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response getListMD5()
    {
        return Response.status( Response.Status.OK ).entity( JsonUtil.toJson( bazaar.getChecksum() ) ).build();
    }


    public void setBazaar( final Bazaar bazaar )
    {
        this.bazaar = bazaar;
    }


    private boolean isHubUser()
    {
        return identityManager.getActiveUser().isHubUser();
    }
}
