package io.subutai.core.hubmanager.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.api.HubPluginException;
import io.subutai.core.hubmanager.rest.pojo.RegistrationPojo;
import io.subutai.core.peer.api.PeerManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );
    private HubManager integration;
    private CommandExecutor commandExecutor;
    private PeerManager peerManager;


    public void setIntegration( final HubManager hubManager )
    {
        this.integration = hubManager;
    }


    @Override
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
                    entity( JsonUtil.GSON.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response triggerHeartbeat()
    {
        integration.triggerHeartbeat();

        return Response.noContent().build();
    }


    @Override
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
                    entity( JsonUtil.GSON.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
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
                    entity( JsonUtil.GSON.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
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
                    entity( JsonUtil.GSON.toJson( e.getMessage() ) ).build();
        }
        return Response.ok().build();
    }


    @Override
    public Response getRegistrationState()
    {
        RegistrationPojo pojo = new RegistrationPojo();

        if ( integration.getRegistrationState() )
        {
            pojo.setOwnerId( integration.getHubConfiguration().getOwnerId() );

            pojo.setOwnerEmail( integration.getHubConfiguration().getOwnerEmail() );
        }

        pojo.setRegisteredToHub( integration.getRegistrationState() );

        String hubRegistrationInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( hubRegistrationInfo ).build();
    }


    @Override
    public Response upSite()
    {

        Thread thread = new Thread()
        {
            public void run()
            {
                VEHServiceImpl.upSite( peerManager );
            }
        };

        thread.start();

        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response downSite()
    {

        Thread thread = new Thread()
        {
            public void run()
            {
                VEHServiceImpl.downSite( peerManager );
            }
        };

        thread.start();

        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response checksum()
    {
        return VEHServiceImpl.getChecksum( peerManager );
    }


    public CommandExecutor getCommandExecutor()
    {
        return commandExecutor;
    }


    public void setCommandExecutor( final CommandExecutor commandExecutor )
    {
        this.commandExecutor = commandExecutor;
    }


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }
}
