package org.safehaus.subutai.core.identity.impl;


import org.apache.shiro.authc.AuthenticationToken;


public class UserToken implements AuthenticationToken
{
    private final String tokenId;


    public UserToken( final String tokenId )
    {
        this.tokenId = tokenId;
    }


    @Override
    public Object getPrincipal()
    {
        return null;
    }


    @Override
    public Object getCredentials()
    {
        return tokenId;
    }
}
