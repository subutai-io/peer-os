package org.safehaus.subutai.core.jetty.fragment;


import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( TestSslContextFactory.class.getName() );

    private static UUID id;

    private static X509KeyManager keyManager;
    private static X509TrustManager trustManager;


    public TestSslContextFactory()
    {
        super();
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
    }


    public static void DO_IT()
    {
        LOG.error( "THE ID >>>> " + id.toString() );
    }


    private String keyStorePath;
    private String keyStorePassword;
    private String trustStorePath;
    private String trustStorePassword;


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        //        return super.getKeyManagers( keyStore );
        return new KeyManager[] {
                keyManager
        };//new KeyManager[] { new CustomKeyManager( keyStorePath, keyStorePassword ) };
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        //        return super.getTrustManagers( trustStore, crls );
        return new TrustManager[] { trustManager };
    }


    @Override
    public void setKeyStorePath( final String keyStorePath )
    {
        LOG.warn( String.format( "KeyStorePath %s", keyStorePath ) );
        super.setKeyStorePath( keyStorePath );
        this.keyStorePath = keyStorePath;
    }


    @Override
    public void setTrustStore( final String trustStorePath )
    {
        LOG.warn( String.format( "TrustStorePath %s", trustStorePath ) );
        super.setTrustStore( trustStorePath );
        this.trustStorePath = trustStorePath;
    }


    @Override
    public void setKeyStorePassword( final String password )
    {
        LOG.warn( String.format( "KeyStorePassword: %s", password ) );
        super.setKeyStorePassword( password );
        this.keyStorePassword = password;
    }


    @Override
    public void setTrustStorePassword( final String password )
    {
        LOG.warn( String.format( "TrustStore password %s", password ) );
        super.setTrustStorePassword( password );
        this.trustStorePassword = password;
    }
}
