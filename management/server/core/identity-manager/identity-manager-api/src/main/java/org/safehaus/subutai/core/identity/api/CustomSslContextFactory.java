package org.safehaus.subutai.core.identity.api;


/**
 * Created by talas on 3/3/15.
 */
public interface CustomSslContextFactory
{
    public void reloadKeyStore();

    public void reloadTrustStore();
}
