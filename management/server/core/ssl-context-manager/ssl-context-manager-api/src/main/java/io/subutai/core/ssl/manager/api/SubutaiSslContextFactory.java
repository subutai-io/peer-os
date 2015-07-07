package io.subutai.core.ssl.manager.api;


public interface SubutaiSslContextFactory
{
    public void reloadKeyStore();

    public void reloadTrustStore();

    public Object getSSLContext();
}