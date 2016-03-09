package io.subutai.core.hubmanager.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.api.Integration;
import io.subutai.core.hubmanager.rest.pojo.RegistrationPojo;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private Integration integration;


    public void setIntegration( final Integration integration )
    {
        this.integration = integration;
    }


    public Response sendHeartbeat( final String hubIp )
    {
        try
        {
            integration.sendHeartbeat();
            return Response.ok().build();
        }
        catch ( HubPluginException e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( JsonUtil.GSON.toJson (e.getMessage()) ).build();
        }
    }


    public Response register( final String hubIp, final String email, final String password )
    {
        try
        {
            integration.registerPeer( hubIp, email, password );
            return Response.ok().build();
        }
        catch ( HubPluginException e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( JsonUtil.GSON.toJson (e.getMessage()) ).build();
        }
    }


    public Response sendRHConfigurations( final String hubIp )
    {
        try
        {
            integration.sendResourceHostInfo();
            return Response.ok().build();
        }
        catch ( HubPluginException e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( JsonUtil.GSON.toJson (e.getMessage()) ).build();
        }
    }


    public Response getHubDns()
    {
        try
        {
            return Response.ok( integration.getHubDns() ).build();
        }
        catch ( HubPluginException e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.BAD_REQUEST ).
                    entity( "Could not get Hub IP" ).build();
        }
    }


    @Override
    public Response unregister()
    {
        try
        {
            integration.unregisterPeer();
        }
        catch ( HubPluginException e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( JsonUtil.GSON.toJson (e.getMessage()) ).build();
        }
        return Response.ok().build();
    }


    @Override
    public Response getRegistrationState()
    {
        RegistrationPojo pojo = new RegistrationPojo();
        pojo.setRegisteredToHub( integration.getRegistrationState() );
        String hubRegistrationInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( hubRegistrationInfo ).build();
    }
}
