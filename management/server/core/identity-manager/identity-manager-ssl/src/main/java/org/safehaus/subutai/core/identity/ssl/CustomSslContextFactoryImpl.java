package org.safehaus.subutai.core.identity.ssl;


import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.safehaus.subutai.core.identity.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSslContextFactoryImpl implements CustomSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );

    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;

    private KeyManager keyManager[];
    private TrustManager trustManager[];

    public CustomSslContextFactoryImpl()
    {
        keyManager = new KeyManager[] { new CustomKeyManager( keyStorePath, keyStorePassword ) };
        trustManager = new TrustManager[] { new CustomTrustManager( trustStorePath, trustStorePassword ) };
    }


    @Override
    public void reloadKeyStore()
    {
        keyManager = new KeyManager[] { new CustomKeyManager( keyStorePath, keyStorePassword ) };
    }


    @Override
    public void reloadTrustStore()
    {
        trustManager = new TrustManager[] { new CustomTrustManager( trustStorePath, trustStorePassword ) };
    }
}
