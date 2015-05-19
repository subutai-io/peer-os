package org.safehaus.subutai.core.ssl.manager.impl;


import org.safehaus.subutai.core.jetty.fragment.SslContextFactoryFragment;
import org.safehaus.subutai.core.ssl.manager.api.SubutaiSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SubutaiSslContextFactoryImpl implements SubutaiSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( SubutaiSslContextFactoryImpl.class );


    public SubutaiSslContextFactoryImpl()
    {
        LOG.error( String.format( "Printing singleton: %s", SslContextFactoryFragment.getLastInstance() ) );
    }


    @Override
    public void reloadKeyStore()
    {
        SslContextFactoryFragment.getLastInstance().reloadStores();
    }


    @Override
    public void reloadTrustStore()
    {
        SslContextFactoryFragment.getLastInstance().reloadStores();
    }


    @Override
    public Object getSSLContext()
    {
        return SslContextFactoryFragment.getLastInstance();
    }
}
