package io.subutai.core.identity.rest;


import java.net.URLDecoder;

import javax.ws.rs.core.Response;

import com.google.common.base.Strings;

import io.subutai.common.security.exception.IdentityExpiredException;
import io.subutai.common.security.exception.InvalidLoginException;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.rest.model.AuthMessage;


public class RestServiceImpl implements RestService
{
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public String createTokenPOST( final String userName, final String password )
    {
        String token = identityManager.getUserToken( userName, password );

        if ( !Strings.isNullOrEmpty( token ) )
        {
            return token;
        }
        else
        {
            return "Access Denied to the resource!";
        }
    }


    @Override
    public String createTokenGET( final String userName, final String password )
    {
        return createTokenPOST( userName, password );
    }



    @Override
    public Response authenticate( int type, String userName, String password )
    {
        try
        {
            password = URLDecoder.decode( password, "UTF-8" );
            String token = identityManager.getUserToken( userName, password );

            if ( !Strings.isNullOrEmpty( token ) )
            {
                AuthMessage authM = new AuthMessage();
                authM.setToken( token );
                return Response.ok( authM ).build();
            }
            else
            {
                return Response.status( Response.Status.FORBIDDEN ).build();
            }

        }
        catch(IdentityExpiredException e)
        {
            User user = identityManager.getUserByUsername( userName );

            if(user != null)
            {
                AuthMessage authM = new AuthMessage();
                authM.setStatus( 1 );
                authM.setAuthId( identityManager.updateUserAuthId( user, null ) );
                return Response.ok( authM ).build();
            }
            else
            {
                return Response.status( Response.Status.NOT_FOUND ).build();
            }
        }
        catch(Exception e)
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

    }


    @Override
    public Response updateAuthId( int type,String userName,String password ,String authId  )
    {
        try
        {
            password = URLDecoder.decode( password, "UTF-8" );
            User user = identityManager.authenticateByAuthSignature( userName, password );

            if(user != null)
            {
                identityManager.updateUserAuthId( user, authId );
                return Response.ok().build();
            }
            else
            {
                throw new InvalidLoginException( "User not found" );
            }
        }
        catch(IdentityExpiredException e)
        {
            User user = identityManager.getUserByUsername( userName );

            if(user != null)
            {
                identityManager.updateUserAuthId( user, authId );
                return Response.ok().build();
            }
            else
            {
                throw new InvalidLoginException( "User not found" );
            }
        }
        catch(Exception e)
        {
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }

    }


    @Override
    public Response getAuthId( int type, String userName, String password )
    {
        try
        {
            password = URLDecoder.decode( password, "UTF-8" );
            User user = identityManager.authenticateByAuthSignature( userName, password );

            if ( user != null )
            {
                return Response.ok(user.getAuthId()).build();
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
            return Response.status( Response.Status.INTERNAL_SERVER_ERROR ).build();
        }
    }

}