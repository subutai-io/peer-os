package org.safehaus.subutai.core.jetty.fragment;


import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( TestSslContextFactory.class.getName() );

    private static UUID id;

    private static KeyManager keyManager[];
    private static TrustManager trustManager[];


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


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        if ( keyManager == null )
        {
            return super.getKeyManagers( keyStore );
        }
        return keyManager;
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        if ( trustManager == null )
        {
            return super.getTrustManagers( trustStore, crls );
        }
        return trustManager;
    }


    public static void setKeyManager( final KeyManager[] keyManager )
    {
        TestSslContextFactory.keyManager = keyManager;
    }


    public static void setTrustManager( final TrustManager[] trustManager )
    {
        TestSslContextFactory.trustManager = trustManager;
    }


    @Override
    public void setKeyStorePath( final String keyStorePath )
    {
        LOG.warn( String.format( "KeyStorePath %s", keyStorePath ) );
        super.setKeyStorePath( keyStorePath );
    }


    @Override
    public void setTrustStore( final String trustStorePath )
    {
        LOG.warn( String.format( "TrustStorePath %s", trustStorePath ) );
        super.setTrustStore( trustStorePath );
    }


    @Override
    public void setKeyStorePassword( final String password )
    {
        LOG.warn( String.format( "KeyStorePassword: %s", password ) );
        super.setKeyStorePassword( password );
    }


    @Override
    public void setTrustStorePassword( final String password )
    {
        LOG.warn( String.format( "TrustStore password %s", password ) );
        super.setTrustStorePassword( password );
    }
}
