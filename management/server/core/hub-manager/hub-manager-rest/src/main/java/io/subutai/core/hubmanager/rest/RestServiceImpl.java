package io.subutai.core.hubmanager.rest;


import java.security.AccessControlException;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.core.executor.api.CommandExecutor;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.rest.pojo.RegistrationPojo;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private HubManager hubManager;

    private CommandExecutor commandExecutor;

    private PeerManager peerManager;

    private IdentityManager identityManager = null;


    public void setIntegration( HubManager hubManager )
    {
        this.hubManager = hubManager;
    }


    @Override
    public Response sendHeartbeat( String hubIp )
    {
        try
        {
            hubManager.sendHeartbeat();

            return Response.ok().build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                    entity( JsonUtil.GSON.toJson( e.getMessage() ) ).build();
        }
    }


    @Override
    public Response triggerHeartbeat()
    {
        hubManager.triggerHeartbeat();

        return Response.noContent().build();
    }


    @Override
    public Response register( final String hubIp, final String email, final String password, final String peerName )
    {
        try
        {
            hubManager.registerPeer( hubIp, email, password, peerName );
            return Response.ok().build();
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

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
            hubManager.sendResourceHostInfo();
            return Response.ok().build();
        }
        catch ( Exception e )
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
            return Response.ok( hubManager.getHubDns() ).build();
        }
        catch ( Exception e )
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
            hubManager.unregisterPeer();
        }
        catch ( Exception e )
        {
            if ( e.getClass() == AccessControlException.class )
            {
                LOG.error( e.getMessage() );
                return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).
                        entity( JsonUtil.GSON.toJson( "You don't have permission to perform this operation" ) ).build();
            }

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

        if ( hubManager.isRegistered() )
        {
            pojo.setOwnerId( hubManager.getHubConfiguration().getOwnerId() );

            pojo.setCurrentUserEmail( hubManager.getCurrentUserEmail() );

            pojo.setPeerName(hubManager.getPeerName());
        }

        pojo.setRegisteredToHub( hubManager.isRegistered() );

        String hubRegistrationInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( hubRegistrationInfo ).build();
    }


    @Override
    public Response upSite()
    {

        Thread thread = new Thread()
        {
            @Override
            public void run()
            {
                VEHServiceUtil.upSite( peerManager, identityManager );
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
            @Override
            public void run()
            {
                VEHServiceUtil.downSite( peerManager, identityManager );
            }
        };

        thread.start();

        return Response.status( Response.Status.OK ).build();
    }


    @Override
    public Response checksum()
    {
        return VEHServiceUtil.getChecksum( peerManager );
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


    public IdentityManager getIdentityManager()
    {
        return identityManager;
    }


    public void setIdentityManager( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }
}
