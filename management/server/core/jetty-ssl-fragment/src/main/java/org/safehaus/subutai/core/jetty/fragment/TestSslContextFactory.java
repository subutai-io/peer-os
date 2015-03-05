package org.safehaus.subutai.core.jetty.fragment;


import java.io.IOException;
import java.security.KeyStore;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.UUID;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.util.security.Password;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TestSslContextFactory extends SslContextFactory
{
    private static Logger LOG = LoggerFactory.getLogger( TestSslContextFactory.class.getName() );

    private static UUID id;

    private static TestSslContextFactory singleton;

    private boolean customStart = false;

    private Password _keyStorePassword;
    private Password _trustStorePassword;


    public TestSslContextFactory()
    {
        super();
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        //        TestSslContextFactory.state = getState();
        //        TestSslContextFactory.secureRandom = getSecureRandomAlgorithm();
        TestSslContextFactory.singleton = this;
    }


    public TestSslContextFactory( final boolean trustAll )
    {
        super( trustAll );
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.singleton = this;
    }


    public TestSslContextFactory( final String keyStorePath )
    {
        super( keyStorePath );
        id = UUID.randomUUID();
        LOG.error( "CUSTOM SSL FACTORY!!!!! " + id.toString() );
        TestSslContextFactory.singleton = this;
    }


    public static TestSslContextFactory getSingleton()
    {
        return singleton;
    }


    public static void DO_IT()
    {
        LOG.error( "THE ID >>>> " + id.toString() );
    }


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        LOG.warn( "getKeyManagers" );
        return super.getKeyManagers( keyStore );
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        return super.getTrustManagers( trustStore, crls );
    }


    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
        //        if ( !customStart )
        //        {
        //            super.doStart();
        //        }
        //        else
        //        {
        //            // verify that keystore and truststore
        //            // parameters are set up correctly
        //            checkKeyStore();
        //
        //            KeyStore keyStore = loadKeyStore();
        //            KeyStore trustStore = loadTrustStore();
        //
        //            Collection<? extends CRL> crls = loadCRL( getCrlPath() );
        //
        //            if ( getValidateCerts() && keyStore != null )
        //            {
        //                if ( getCertAlias() == null )
        //                {
        //                    List<String> aliases = Collections.list( keyStore.aliases() );
        //                    setCertAlias( aliases.size() == 1 ? aliases.get( 0 ) : null );
        //                }
        //
        //                Certificate cert = getCertAlias() == null ? null : keyStore.getCertificate( getCertAlias() );
        //                if ( cert == null )
        //                {
        //                    throw new Exception( "No certificate found in the keystore" + ( getCertAlias() == null
        // ? "" :
        //                                                                                    " for alias " +
        // getCertAlias() ) );
        //                }
        //
        //                CertificateValidator validator = new CertificateValidator( trustStore, crls );
        //                validator.setMaxCertPathLength( getMaxCertPathLength() );
        //                validator.setEnableCRLDP( isEnableCRLDP() );
        //                validator.setEnableOCSP( isEnableOCSP() );
        //                validator.setOcspResponderURL( getOcspResponderURL() );
        //                validator.validate( keyStore, cert );
        //            }
        //
        //            KeyManager[] keyManagers = getKeyManagers( keyStore );
        //            TrustManager[] trustManagers = getTrustManagers( trustStore, crls );
        //
        //            SecureRandom secureRandom = ( getSecureRandomAlgorithm() == null ) ? null : SecureRandom
        // .getInstance(
        //                    getSecureRandomAlgorithm() );
        //            setSslContext( ( getProvider() == null ) ? SSLContext.getInstance( getProtocol() ) : SSLContext
        // .getInstance( getProtocol(), getProvider() ) );
        //
        //            setCustomStart( true );
        //            getSslContext().init( keyManagers, trustManagers, secureRandom );
        //            setCustomStart( false );
        //
        //            SSLEngine engine = newSslEngine();
        //
        //            LOG.info( "Enabled Protocols {} of {}", Arrays.asList( engine.getEnabledProtocols() ), Arrays
        // .asList( engine.getSupportedProtocols() ) );
        //            if ( LOG.isDebugEnabled() )
        //            {
        //                LOG.debug( "Enabled Ciphers   {} of {}", Arrays.asList( engine.getEnabledCipherSuites() ),
        // Arrays.asList( engine.getSupportedCipherSuites() ) );
        //            }
        //        }
    }


    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        //        if ( customStart )
        //        {
        //            stop();
        //            super.doStop();
        //        }
        //        else
        //        {
        //            super.doStop();
        //        }
    }


    public static void setKeyManager( final KeyManager[] keyManager )
    {
        //        TestSslContextFactory.keyManager = keyManager;
    }


    public static void setTrustManager( final TrustManager[] trustManager )
    {
        //        TestSslContextFactory.trustManager = trustManager;
    }


    public void reloadStores()
    {
        try
        {
            doStop();

            Thread.sleep( 1000 );

            doStart();
        }
        catch ( Exception e )
        {
            LOG.error( "Error restarting ssl context factory", e );
        }
    }


    public void setCustomStart( final boolean customStart )
    {
        this.customStart = customStart;
    }


    //    Parent Methods


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


    //Critically important for reloading keyStores, trustStores
    @Override
    public boolean isStarted()
    {
        //        if ( customStart )
        //        {
        //            return true;
        //        }
        return super.isStarted();
    }


    @Override
    public SSLServerSocket newSslServerSocket( final String host, final int port, final int backlog ) throws IOException
    {
        return super.newSslServerSocket( host, port, backlog );
    }


    @Override
    public SSLSocket newSslSocket() throws IOException
    {
        return super.newSslSocket();
    }


    @Override
    public SSLEngine newSslEngine( final String host, final int port )
    {
        //        try
        //        {
        //            getSslContext().init( TestSslContextFactory.keyManager, TestSslContextFactory.trustManager,
        // null );
        //        }
        //        catch ( KeyManagementException e )
        //        {
        //            LOG.error( "Error initializing ssl engine", e );
        //        }
        return super.newSslEngine( host, port );
    }


    @Override
    public SSLEngine newSslEngine()
    {
        //        try
        //        {
        //            getSslContext().init( TestSslContextFactory.keyManager, TestSslContextFactory.trustManager,
        // null );
        //        }
        //        catch ( KeyManagementException e )
        //        {
        //            LOG.error( "Error initializing ssl engine", e );
        //        }
        return super.newSslEngine();
    }


    @Override
    protected KeyStore loadKeyStore() throws Exception
    {
        //        if ( customStart )
        //        {
        //            return getKeyStore( getKeyStoreInputStream(), getKeyStorePath(), getKeyStoreType(),
        // getKeyStoreProvider(),
        //                    _keyStorePassword == null ? null : _keyStorePassword.toString() );
        //        }
        return super.loadKeyStore();
    }


    @Override
    protected KeyStore loadTrustStore() throws Exception
    {
        //        if ( customStart )
        //        {
        //            return getKeyStore( getTrustStoreInputStream(), getTrustStore(), getTrustStoreType(),
        //                    getTrustStoreProvider(), _trustStorePassword == null ? null : _trustStorePassword
        // .toString() );
        //        }
        return super.loadTrustStore();
    }


    @Override
    public void setKeyStorePassword( final String password )
    {
        checkNotStarted();

        _keyStorePassword = Password.getPassword( PASSWORD_PROPERTY, password, null );
    }


    @Override
    public void setTrustStorePassword( final String password )
    {
        checkNotStarted();

        _trustStorePassword = Password.getPassword( PASSWORD_PROPERTY, password, null );
    }
}
