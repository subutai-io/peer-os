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
    public String getToken( final String userName, final String password )
    {
        return identityManager.getUserToken( userName, password );
    }
}