package org.safehaus.subutai.core.identity.ssl;


import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
//import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;


public class CustomSslContextFactoryImpl extends SslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );

    //    <Set name="KeyStore">/var/lib/subutai/keystores/keystore_server_px2.jks</Set>
    //    <Set name="KeyStorePassword">subutai</Set>
    //    <Set name="certAlias">root_server_px2</Set>
    //    <Set name="TrustStore">/var/lib/subutai/keystores/truststore_server_px2.jks</Set>
    //    <Set name="TrustStorePassword">subutai</Set>
    //    <Set name="needClientAuth">true</Set>
    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;


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
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        return super.getKeyManagers( keyStore );
        //        return new KeyManager[] { new CustomKeyManager( keyStorePath, keyStorePassword ) };
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        return super.getTrustManagers( trustStore, crls );
        //        return new TrustManager[] { new CustomTrustManager( trustStorePath, trustStorePassword ) };
    }


    @Override
    public void setKeyStorePath( final String keyStorePath )
    {
        super.setKeyStorePath( keyStorePath );
        this.keyStorePath = keyStorePath;
    }


    @Override
    public void setTrustStore( final String trustStorePath )
    {
        LOG.debug( String.format( "TrustStorePath %s", trustStorePath ) );
        super.setTrustStore( trustStorePath );
        this.trustStorePath = trustStorePath;
    }


    @Override
    public void setKeyStorePassword( final String password )
    {
        LOG.debug( String.format( "KeyStorePassword: %s", password ) );
        super.setKeyStorePassword( password );
        this.keyStorePassword = password;
    }


    @Override
    public void setTrustStorePassword( final String password )
    {
        LOG.debug( String.format( "TrustStore password %s", password ) );
        super.setTrustStorePassword( password );
        this.trustStorePassword = password;
    }
}
