package io.subutai.core.pluginmanager.rest;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import com.google.common.base.Strings;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.pluginmanager.api.PluginManager;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private static final String ERROR_KEY = "ERROR";
    private PluginManager pluginManager;
    private IdentityManager identityManager;


    @Override
    public javax.ws.rs.core.Response uploadPlugin( final String name, final String version, final Attachment kar,
                                                   final String permissionJson )
    {
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
            File karFile = new File( System.getProperty( "karaf.home" ) + "/deploy/" + name + ".kar" );
            
            if ( !karFile.createNewFile() )
            {
                LOG.info( "Plugin {} already exists. Overwriting", name );
            }

            kar.transferTo( karFile );

            pluginManager.register( name, version, karFile.getAbsolutePath(), permissions );
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
        try
        {
            pluginManager.unregister( Long.parseLong( pluginId ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error deleting profile {}", e.getMessage() );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

        return Response.status( Response.Status.OK ).build();
    }


    //todo implement or remove
    public Response setPermissions( final String pluginId, final String permissionJson )
    {
        return Response.status( Response.Status.OK ).build();
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    public void setPluginManager( final PluginManager pluginManager )
    {
        this.pluginManager = pluginManager;
    }
}
