package io.subutai.core.hubmanager.rest;


import java.security.AccessControlException;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.hubmanager.api.HubManager;
import io.subutai.core.hubmanager.rest.pojo.RegistrationPojo;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private HubManager hubManager;


    @Override
    public Response sendHeartbeat()
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
    public Response register( final String email, final String password, final String peerName, final String peerScope )
    {
        try
        {
            hubManager.registerPeer( email, password, StringUtil.removeHtml( peerName ), peerScope );

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
                    entity( "Could not get Bazaar IP" ).build();
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

        if ( hubManager.isRegisteredWithHub() )
        {
            pojo.setOwnerId( hubManager.getHubConfiguration().getOwnerId() );

            pojo.setCurrentUserEmail( hubManager.getCurrentUserEmail() );

            pojo.setPeerName( hubManager.getPeerName() );
        }

        pojo.setRegisteredToHub( hubManager.isRegisteredWithHub() );

        pojo.setHubReachable( hubManager.isHubReachable() );

        String hubRegistrationInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( hubRegistrationInfo ).build();
    }


    public void setIntegration( HubManager hubManager )
    {
        this.hubManager = hubManager;
    }
}
