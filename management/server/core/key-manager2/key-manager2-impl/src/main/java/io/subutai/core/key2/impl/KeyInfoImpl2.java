package io.subutai.core.key2.impl;

import io.subutai.core.key2.api.KeyInfo2;

import java.util.Set;

/**
 * Created by caveman on 21.07.2015.
 */
public class KeyInfoImpl2 implements KeyInfo2{

    private final String realName;
    private final String email;
    private final String publicKeyId;
    private final Set<String> subKeyIds;

    public KeyInfoImpl2(String realName, String email, String publicKeyId, Set<String> subKeyIds) {
        this.realName = realName;
        this.email = email;
        this.publicKeyId = publicKeyId;
        this.subKeyIds = subKeyIds;
    }

    @Override
    public String getRealName() {
        return null;
    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getPublicKeyId() {
        return null;
    }

    @Override
    public Set<String> getSubKeyIds() {
        return null;
    }
}
