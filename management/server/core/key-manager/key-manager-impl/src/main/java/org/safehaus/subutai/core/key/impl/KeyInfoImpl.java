package org.safehaus.subutai.core.key.impl;


import java.util.Set;

import org.safehaus.subutai.core.key.api.KeyInfo;


/**
 * Implementation of KeyInfo
 */
public class KeyInfoImpl implements KeyInfo
{

    private final String realName;
    private final String email;
    private final String publicKeyId;
    private final Set<String> subKeyIds;


    public KeyInfoImpl( final String realName, final String email, final String publicKeyId,
                        final Set<String> subKeyIds )
    {
        this.realName = realName;
        this.email = email;
        this.publicKeyId = publicKeyId;
        this.subKeyIds = subKeyIds;
    }


    @Override
    public String getRealName()
    {
        return null;
    }


    @Override
    public String getEmail()
    {
        return null;
    }


    @Override
    public String getPublicKeyId()
    {
        return null;
    }


    @Override
    public Set<String> getSubKeyIds()
    {
        return null;
    }
}
