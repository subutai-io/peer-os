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
        LOG.error( String.format( "Printing singleton: %s", SslContextFactoryFragment.getSingleton() ) );
    }


    @Override
    public void reloadKeyStore()
    {
        SslContextFactoryFragment.getSingleton().reloadStores();
    }


    @Override
    public void reloadTrustStore()
    {
        SslContextFactoryFragment fragment = SslContextFactoryFragment.getSingleton();
        if ( fragment != null )
        {
            fragment.reloadStores();
        }
    }


    @Override
    public Object getSSLContext()
    {
        return SslContextFactoryFragment.getSingleton();
    }
}
