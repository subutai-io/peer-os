package io.subutai.core.key.impl;


import java.util.Collections;
import java.util.Set;

import org.safehaus.subutai.common.util.CollectionUtil;
import io.subutai.core.key.api.KeyInfo;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


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
        Preconditions.checkArgument( !Strings.isNullOrEmpty( realName ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( email ) );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( publicKeyId ) );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( subKeyIds ) );

        this.realName = realName;
        this.email = email;
        this.publicKeyId = publicKeyId;
        this.subKeyIds = subKeyIds;
    }


    @Override
    public String getRealName()
    {
        return realName;
    }


    @Override
    public String getEmail()
    {
        return email;
    }


    @Override
    public String getPublicKeyId()
    {
        return publicKeyId;
    }


    @Override
    public Set<String> getSubKeyIds()
    {
        return Collections.unmodifiableSet( subKeyIds );
    }


    @Override
    public String toString()
    {
        return "KeyInfoImpl{" +
                "realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", publicKeyId='" + publicKeyId + '\'' +
                ", subKeyIds=" + subKeyIds +
                '}';
    }
}
