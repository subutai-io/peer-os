package org.safehaus.subutai.core.identity.ssl;


import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.safehaus.subutai.core.identity.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSslContextFactoryImpl extends SslContextFactory implements CustomSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );

    //    <Set name="KeyStore">/var/lib/subutai/keystores/keystore_server_px2.jks</Set>
    //    <Set name="KeyStorePassword">subutai</Set>
    //    <Set name="certAlias">root_server_px2</Set>
    //    <Set name="TrustStore">/var/lib/subutai/keystores/truststore_server_px2.jks</Set>
    //    <Set name="TrustStorePassword">subutai</Set>
    //    <Set name="needClientAuth">true</Set>


    public CustomSslContextFactoryImpl()
    {
        super();
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    public CustomSslContextFactoryImpl( final boolean trustAll )
    {
        super( trustAll );
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    public CustomSslContextFactoryImpl( final String keyStorePath )
    {
        super( keyStorePath );
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    @Override
    public void reloadKeyStore()
    {
    }


    @Override
    public void reloadTrustStore()
    {
    }


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        KeyStoreManager keyStoreManager = new KeyStoreManager();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        KeyStore keyStore1 = keyStoreManager.load( keyStoreData );
        List<String> aliases = Collections.list( keyStore1.aliases() );
        return super.getKeyManagers( keyStore );
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        return super.getTrustManagers( trustStore, crls );
    }
}
