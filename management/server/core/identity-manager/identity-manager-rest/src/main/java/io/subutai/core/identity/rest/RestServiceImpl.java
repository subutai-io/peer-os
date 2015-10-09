package io.subutai.core.identity.rest;


public class RestServiceImpl implements RestService
{
    private IdentityManager identityManager;


    public RestServiceImpl( final IdentityManager identityManager )
    {
        this.identityManager = identityManager;
    }


    @Override
    public String getKey( final String username )
    {
        return identityManager.getUserKey(username);
    }
}