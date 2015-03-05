package org.safehaus.subutai.core.jetty.fragment;


import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
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
    private static SSLContext sslContext;
    private static String secureRandom;
    private static String state;
    private static TestSslContextFactory singleton;


    public TestSslContextFactory()
    {
        super();
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.state = getState();
        TestSslContextFactory.secureRandom = getSecureRandomAlgorithm();
        TestSslContextFactory.singleton = this;
    }


    public static void DO_IT()
    {
        LOG.error( "THE ID >>>> " + id.toString() );
    }


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        LOG.warn( "getKeyManagers" );
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


    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
        TestSslContextFactory.state = getState();
        new Thread( new Runnable()
        {
            @Override
            public void run()
            {
                while ( !getState().equals( "STARTED" ) )
                {
                    TestSslContextFactory.secureRandom = getSecureRandomAlgorithm();
                    TestSslContextFactory.sslContext = getSslContext();
                    TestSslContextFactory.state = "STARTED";
                    LOG.warn( "Got SSLContext in STARTED State" );
                }
            }
        } ).start();
    }


    public static void setKeyManager( final KeyManager[] keyManager, boolean setContext )
    {
        TestSslContextFactory.keyManager = keyManager;
        if ( setContext )
        {
            try
            {
                SecureRandom secureRandom = ( TestSslContextFactory.secureRandom == null ) ? null :
                                            SecureRandom.getInstance( TestSslContextFactory.secureRandom );
                if ( TestSslContextFactory.sslContext == null )
                {
                    TestSslContextFactory.sslContext
                            .init( TestSslContextFactory.keyManager, TestSslContextFactory.trustManager, secureRandom );
                }
            }
            catch ( KeyManagementException | NoSuchAlgorithmException e )
            {
                LOG.error( "Error initializing sslContext", e );
            }
        }
    }


    public static void setTrustManager( final TrustManager[] trustManager, boolean setContext )
    {
        TestSslContextFactory.trustManager = trustManager;
        if ( setContext )
        {
            try
            {
                SecureRandom secureRandom = ( TestSslContextFactory.secureRandom == null ) ? null :
                                            SecureRandom.getInstance( TestSslContextFactory.secureRandom );
                if ( TestSslContextFactory.sslContext == null )
                {
                    TestSslContextFactory.sslContext
                            .init( TestSslContextFactory.keyManager, TestSslContextFactory.trustManager, secureRandom );
                }
            }
            catch ( KeyManagementException | NoSuchAlgorithmException e )
            {
                LOG.error( "Error initializing sslContext", e );
            }
        }
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


    @Override
    public SSLContext getSslContext()
    {
        LOG.warn( "Getting sslContext." );
        return super.getSslContext();
    }


    @Override
    public void setSslContext( final SSLContext sslContext )
    {
        super.setSslContext( sslContext );
        LOG.warn( "Setting sslContext" );
    }
}
