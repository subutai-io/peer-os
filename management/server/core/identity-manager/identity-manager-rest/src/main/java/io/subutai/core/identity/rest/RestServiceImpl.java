package io.subutai.core.identity.rest;


import io.subutai.core.identity.api.IdentityManager;


public class RestServiceImpl implements RestService
{
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public String createToken( final String userName, final String password )
    {
        return identityManager.getUserToken( userName, password );
    }


    @Override
    public String getToken( final String userName, final String password )
    {
        return createToken( userName, password );
    }


    @Override
    public Token createToken( final UserCrdentials userCrdentials )
    {
        String token = identityManager.getUserToken( userCrdentials.getUsername(), userCrdentials.getPassword() );
        return new Token( token );
    }
}