package io.subutai.core.identity.rest;


import javax.ws.rs.FormParam;
import com.google.common.base.Strings;
import io.subutai.core.identity.api.IdentityManager;


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
    public String authenticate( @FormParam( "type" ) final int type, @FormParam( "username" ) final String userName,
                                @FormParam( "password" ) final String password )
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
    public String getAuthID( @FormParam( "fingerprint" ) final String fingerprint,
                             @FormParam( "signature" ) final String signature )
    {
        return null;
    }
}