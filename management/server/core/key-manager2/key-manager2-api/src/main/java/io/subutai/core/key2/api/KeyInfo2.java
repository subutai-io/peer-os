package io.subutai.core.key2.api;

import java.util.Set;

/**
 * Created by caveman on 21.07.2015.
 */
public interface KeyInfo2 {
    public String getRealName();

    public String getEmail();

    public String getPublicKeyId();

    public Set<String> getSubKeyIds();
}
