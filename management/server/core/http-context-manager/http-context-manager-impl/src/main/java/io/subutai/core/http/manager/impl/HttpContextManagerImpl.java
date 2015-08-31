package io.subutai.core.http.manager.impl;


import io.subutai.core.http.manager.api.HttpContextManager;
import io.subutai.core.http.context.jetty.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HttpContextManagerImpl implements HttpContextManager
{

    private static final Logger LOG = LoggerFactory.getLogger( HttpContextManagerImpl.class );


    public HttpContextManagerImpl()
    {
        LOG.error( String.format( "Printing singleton: %s", CustomSslContextFactory.getLastInstance() ) );
    }


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