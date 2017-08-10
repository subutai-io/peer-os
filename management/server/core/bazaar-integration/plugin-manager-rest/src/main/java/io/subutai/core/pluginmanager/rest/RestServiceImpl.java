package io.subutai.core.pluginmanager.rest;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.pluginmanager.api.PluginManager;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String ERROR_KEY = "ERROR";
    private PluginManager pluginManager;
    private IdentityManager identityManager = ServiceLocator.lookup( IdentityManager.class );

    private boolean isHubUser()
    {
        return identityManager.getActiveUser().isHubUser();
    }

    @Override
    public javax.ws.rs.core.Response uploadPlugin( final String name, final String version, final Attachment kar,
                                                   final String permissionJson )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        ArrayList<PermissionJson> permissions = null;
        if ( !Strings.isNullOrEmpty( permissionJson ) )
        {
            permissions = JsonUtil.fromJson( permissionJson, new TypeToken<ArrayList<PermissionJson>>()
            {
            }.getType() );
        }

        if ( kar == null )
        {
            return javax.ws.rs.core.Response.status( javax.ws.rs.core.Response.Status.BAD_REQUEST ).build();
        }

        try
        {
            pluginManager.register( name, version, kar, permissions );
        }
        catch ( IOException e )
        {
            return javax.ws.rs.core.Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                                            .build();
        }
        return Response.ok().build();
    }


    @Override
    public Response installedPlugins()
    {
        List<PluginDetails> pluginList = pluginManager.getInstalledPlugins();
        String plugins = JsonUtil.toJson( pluginList );
        return Response.status( Response.Status.OK ).entity( plugins ).build();
    }


    @Override
    public Response getPluginDetails( final String pluginId )
    {
        PluginDetails pluginInfo = pluginManager.getConfigDataService().getPluginDetails( Long.parseLong( pluginId ) );
        return Response.status( Response.Status.OK ).entity( pluginInfo ).build();
    }


    @Override
    public Response deleteProfile( final String pluginId )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        try
        {
            pluginManager.unregister( Long.parseLong( pluginId ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error deleting profile {}", e.getMessage() );

            return javax.ws.rs.core.Response.serverError().entity( JsonUtil.toJson( ERROR_KEY, e.getMessage() ) )
                                            .build();
        }

        return Response.status( Response.Status.OK ).build();
    }


    //todo implement or remove
    @Override
    public Response setPermissions( final String pluginId, final String permissionJson )
    {
        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response updatePlugin( final String pluginId, final String name, final String version )
    {
        if ( isHubUser() )
        {
            return Response.status( Response.Status.FORBIDDEN ).
                    entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
        }

        pluginManager.update( pluginId, name, version );
        return null;
    }


    public void setPluginManager( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }
}
