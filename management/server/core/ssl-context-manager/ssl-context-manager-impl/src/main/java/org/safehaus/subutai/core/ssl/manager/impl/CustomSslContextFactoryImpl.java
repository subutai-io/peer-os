package org.safehaus.subutai.core.ssl.manager.impl;


import org.safehaus.subutai.core.jetty.fragment.TestSslContextFactory;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSslContextFactoryImpl implements CustomSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );


    public CustomSslContextFactoryImpl()
    {
        LOG.error( String.format( "Printing singleton: %s", TestSslContextFactory.getSingleton() ) );
    }


    @Override
    public void reloadKeyStore()
    {
        //        TestSslContextFactory.setKeyManager( keyManager );
        TestSslContextFactory.getSingleton().reloadStores();
    }


    @Override
    public void reloadTrustStore()
    {
        //        TestSslContextFactory.setTrustManager( trustManager );
        TestSslContextFactory.getSingleton().reloadStores();
    }


    @Override
    public Object getSSLContext()
    {
        return TestSslContextFactory.getSingleton();
    }
}
