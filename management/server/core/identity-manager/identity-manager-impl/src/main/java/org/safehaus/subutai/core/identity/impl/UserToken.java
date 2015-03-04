package org.safehaus.subutai.core.identity.impl;


import org.apache.shiro.authc.AuthenticationToken;


public class UserToken implements AuthenticationToken
{
    private final String tokenId;
    private final String ip;


    public UserToken( final String tokenId, final String ip )
    {
        this.tokenId = tokenId;
        this.ip = ip;
    }


    public String getIp()
    {
        return ip;
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
