package org.safehaus.subutai.core.ssl.manager.api;


//import org.safehaus.subutai.core.jetty.fragment.TestSslContextFactory;


public interface SubutaiSslContextFactory
{
    public void reloadKeyStore();

    public void reloadTrustStore();

    public Object getSSLContext();
}