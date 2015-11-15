package io.subutai.core.security.impl.jetty;


import io.subutai.core.http.context.jetty.CustomSslContextFactory;
import io.subutai.core.security.api.jetty.HttpContextManager;


public class HttpContextManagerImpl implements HttpContextManager
{


    @Override
    public void reloadKeyStore()
    {
        CustomSslContextFactory.getLastInstance().reloadStores();
    }


    @Override
    public void reloadTrustStore()
    {
        CustomSslContextFactory.getLastInstance().reloadStores();
    }


    @Override
    public Object getSSLContext()
    {
        return CustomSslContextFactory.getLastInstance();
    }
}