package org.safehaus.subutai.core.identity.impl;


import org.apache.shiro.authc.AuthenticationToken;


public class UserToken implements AuthenticationToken
{
    private final String username;


    public UserToken( final String username )
    {
        this.username = username;
    }


    @Override
    public Object getPrincipal()
    {
        return username;
    }


    @Override
    public Object getCredentials()
    {
        return null;
    }
}
