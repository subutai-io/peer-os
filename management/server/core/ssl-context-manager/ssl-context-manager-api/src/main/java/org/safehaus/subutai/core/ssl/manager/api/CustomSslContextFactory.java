package org.safehaus.subutai.core.ssl.manager.api;


public interface CustomSslContextFactory
{
    public void reloadKeyStore();

    public void reloadTrustStore();
}