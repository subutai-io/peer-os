package io.subutai.core.identity.rest;


import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.rest.model.AuthMessage;


public class RestServiceImpl implements RestService
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestServiceImpl.class.getName() );


    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public Response createTokenPOST( final String userName, final String password )
    {
        try
        {
            String token = identityManager.getUserToken( userName, password );

            if ( !Strings.isNullOrEmpty( token ) )
            {
                return Response.ok( token ).build();
            }
            else
            {
                return Response.status( Response.Status.UNAUTHORIZED ).entity( "Invalid credentials" ).build();
            }
        }
        catch ( InvalidLoginException e )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).entity( "Invalid credentials" ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage() );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response createTokenGET( final String userName, final String password )
    {
        return createTokenPOST( userName, password );
    }


    @Override
    public Response getSignToken()
    {
        return Response.ok( identityManager.getSignToken() ).build();
    }


    @Override
    public Response authenticate( int type, String userName, String password )
    {
        try
        {
            String token = identityManager.getUserToken( userName, password );

            if ( !Strings.isNullOrEmpty( token ) )
            {
                AuthMessage authM = new AuthMessage();
                authM.setToken( token );
                return Response.ok( JsonUtil.toJson( authM ) ).build();
            }
            else
            {
                return Response.status( Response.Status.UNAUTHORIZED ).entity( "Invalid credentials" ).build();
            }
        }
        catch ( IdentityExpiredException e )
        {

            User user;

            if ( userName.length() == 40 )
            {
                user = identityManager.getUserByFingerprint( userName );
            }
            else
            {
                user = identityManager.getUserByUsername( userName );
            }

            if ( user != null )
            {
                AuthMessage authM = new AuthMessage();
                authM.setStatus( 1 );
                authM.setAuthId( identityManager.updateUserAuthId( user, null ) );
                return Response.ok( JsonUtil.toJson( authM ) ).build();
            }
            else
            {
                return Response.status( Response.Status.UNAUTHORIZED ).build();
            }
        }
        catch ( InvalidLoginException e )
        {
            return Response.status( Response.Status.UNAUTHORIZED ).entity( "Invalid credentials" ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.getMessage() );
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response updateAuthId( int type, String userName, String password, String authId )
    {
        try
        {
            User user = identityManager.authenticateByAuthSignature( userName, password );

            if ( user != null )
            {
                identityManager.updateUserAuthId( user, authId );
                return Response.ok().build();
            }
            else
            {
                throw new InvalidLoginException( "User not found" );
            }
        }
        catch ( IdentityExpiredException e )
        {
            User user;

            if ( userName.length() == 40 )
            {
                user = identityManager.getUserByFingerprint( userName );
            }
            else
            {
                user = identityManager.getUserByUsername( userName );
            }

            if ( user != null )
            {
                identityManager.updateUserAuthId( user, authId );
                return Response.ok().build();
            }
            else
            {
                throw new InvalidLoginException( "User not found" );
            }
        }
        catch ( Exception e )
        {
            LOGGER.error( "***** Error, updating authID:" + e.toString(), e );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }


    @Override
    public Response getAuthId( int type, String userName, String password )
    {
        try
        {
            User user = identityManager.authenticateByAuthSignature( userName, password );

            if ( user != null )
            {
                return Response.ok( user.getAuthId() ).build();
            }
            else
            {
                throw new InvalidLoginException( "User not found" );
            }
        }
        catch ( IdentityExpiredException e )
        {
            return Response.ok( "User credentials are expired" ).build();
        }
        catch ( Exception e )
        {
            LOGGER.error( "***** Error, getting authID:" + e.toString(), e );

            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }
}