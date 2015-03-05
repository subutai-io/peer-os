package org.safehaus.subutai.core.ssl.manager.impl;


import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.safehaus.subutai.core.jetty.fragment.TestSslContextFactory;
import org.safehaus.subutai.core.ssl.manager.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSslContextFactoryImpl implements CustomSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );

    private KeyManager keyManager[];
    private TrustManager trustManager[];


    public CustomSslContextFactoryImpl()
    {
        keyManager = new KeyManager[] { new CustomKeyManager() };
        trustManager = new TrustManager[] { new CustomTrustManager() };
        TestSslContextFactory.setKeyManager( keyManager );
        TestSslContextFactory.setTrustManager( trustManager );
        LOG.error( String.format( "Printing singleton: %s", TestSslContextFactory.getSingleton() ) );
    }


    @Override
    public void reloadKeyStore()
    {
        keyManager = new KeyManager[] { new CustomKeyManager() };
        //        TestSslContextFactory.setKeyManager( keyManager );
        TestSslContextFactory.getSingleton().reloadStores();
    }


    @Override
    public void reloadTrustStore()
    {
        trustManager = new TrustManager[] { new CustomTrustManager() };
        //        TestSslContextFactory.setTrustManager( trustManager );
        TestSslContextFactory.getSingleton().reloadStores();
    }


    @Override
    public Object getSSLContext()
    {
        return TestSslContextFactory.getSingleton();
    }
}
