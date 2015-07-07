package io.subutai.core.key.api;


import java.util.Set;


/**
 * Represents key metadata
 */
public interface KeyInfo
{
    public String getRealName();

    public String getEmail();

    public String getPublicKeyId();

    public Set<String> getSubKeyIds();
}
