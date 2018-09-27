package io.subutai.core.bazaarmanager.rest;


import java.security.AccessControlException;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.StringUtil;
import io.subutai.core.bazaarmanager.api.BazaarManager;
import io.subutai.core.bazaarmanager.rest.pojo.RegistrationPojo;


public class RestServiceImpl implements RestService
{
    private static final Logger LOG = LoggerFactory.getLogger( RestServiceImpl.class.getName() );

    private BazaarManager bazaarManager;


    @Override
    public Response sendHeartbeat()
    {
        try
        {
            bazaarManager.sendHeartbeat();

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
        bazaarManager.triggerHeartbeat();

        return Response.noContent().build();
    }


    @Override
    public Response register( final String email, final String password, final String peerName, final String peerScope )
    {
        try
        {
            bazaarManager.registerPeer( email, password, StringUtil.removeHtml( peerName ), peerScope );

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
    public Response getBazaarIp()
    {
        try
        {
            return Response.ok( bazaarManager.getBazaarIp() ).build();
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
            bazaarManager.unregisterPeer();
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

        if ( bazaarManager.isRegisteredWithBazaar() )
        {
            pojo.setOwnerId( bazaarManager.getBazaarConfiguration().getOwnerId() );

            pojo.setCurrentUserEmail( bazaarManager.getCurrentUserEmail() );

            pojo.setPeerName( bazaarManager.getPeerName() );
        }

        pojo.setRegisteredToBazaar( bazaarManager.isRegisteredWithBazaar() );

        pojo.setBazaarReachable( bazaarManager.isBazaarReachable() );

        String registrationInfo = JsonUtil.GSON.toJson( pojo );

        return Response.status( Response.Status.OK ).entity( registrationInfo ).build();
    }


    public void setIntegration( BazaarManager bazaarManager )
    {
        this.bazaarManager = bazaarManager;
    }
}
