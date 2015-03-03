package org.safehaus.subutai.core.identity.ssl;


import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.CRL;
import java.util.Collection;
import java.util.Enumeration;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.safehaus.subutai.core.identity.api.CustomSslContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CustomSslContextFactoryImpl extends SslContextFactory implements CustomSslContextFactory
{

    private static final Logger LOG = LoggerFactory.getLogger( CustomSslContextFactoryImpl.class );


    /**
     * Construct an instance of SslContextFactory Default constructor for use in XmlConfiguration files
     */
    public CustomSslContextFactoryImpl()
    {
        super();
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    /**
     * Construct an instance of SslContextFactory Default constructor for use in XmlConfiguration files
     *
     * @param trustAll whether to blindly trust all certificates
     *
     * @see #setTrustAll(boolean)
     */
    public CustomSslContextFactoryImpl( final boolean trustAll )
    {
        super( trustAll );
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    /**
     * Construct an instance of SslContextFactory
     *
     * @param keyStorePath default keystore location
     */
    public CustomSslContextFactoryImpl( final String keyStorePath )
    {
        super( keyStorePath );
        LOG.warn( "Subutai Ssl Factory initialized" );
    }


    /**
     * Set the key store.
     *
     * @param keyStore the key store to set
     */
    @Override
    public void setKeyStore( final KeyStore keyStore )
    {
        super.setKeyStore( keyStore );
        LOG.info( "Setting keyStore" );
        LOG.info( "KeyStore aliases" );
        try
        {
            Enumeration<String> aliases = keyStore.aliases();
            while ( aliases.hasMoreElements() )
            {
                String alias = aliases.nextElement();
                LOG.info( alias );
            }
        }
        catch ( KeyStoreException e )
        {
            e.printStackTrace();
        }
    }


    @Override
    protected KeyStore loadKeyStore() throws Exception
    {
        LOG.warn( "Loading keyStore" );
        return super.loadKeyStore();
    }


    @Override
    protected KeyStore loadTrustStore() throws Exception
    {
        LOG.warn( "Loading trustStore" );
        return super.loadTrustStore();
    }


    @Override
    public void reloadKeyStore()
    {
        try
        {
            loadKeyStore();
        }
        catch ( Exception e )
        {
            LOG.error( "Error reloading keyStore", e );
        }
    }


    @Override
    public void reloadTrustStore()
    {
        try
        {
            loadTrustStore();
        }
        catch ( Exception e )
        {
            LOG.error( "Error reloading trustStore", e );
        }
    }


    @Override
    protected void doStart() throws Exception
    {
        super.doStart();
    }


    @Override
    public String[] getExcludeProtocols()
    {
        return super.getExcludeProtocols();
    }


    @Override
    public void setExcludeProtocols( final String... protocols )
    {
        super.setExcludeProtocols( protocols );
    }


    @Override
    public void addExcludeProtocols( final String... protocol )
    {
        super.addExcludeProtocols( protocol );
    }


    @Override
    public String[] getIncludeProtocols()
    {
        return super.getIncludeProtocols();
    }


    @Override
    public void setIncludeProtocols( final String... protocols )
    {
        super.setIncludeProtocols( protocols );
    }


    @Override
    public String[] getExcludeCipherSuites()
    {
        return super.getExcludeCipherSuites();
    }


    @Override
    public void setExcludeCipherSuites( final String... cipherSuites )
    {
        super.setExcludeCipherSuites( cipherSuites );
    }


    @Override
    public void addExcludeCipherSuites( final String... cipher )
    {
        super.addExcludeCipherSuites( cipher );
    }


    @Override
    public String[] getIncludeCipherSuites()
    {
        return super.getIncludeCipherSuites();
    }


    @Override
    public void setIncludeCipherSuites( final String... cipherSuites )
    {
        super.setIncludeCipherSuites( cipherSuites );
    }


    @Override
    public String getKeyStorePath()
    {
        return super.getKeyStorePath();
    }


    @Override
    public String getKeyStore()
    {
        return super.getKeyStore();
    }


    @Override
    public void setKeyStorePath( final String keyStorePath )
    {
        super.setKeyStorePath( keyStorePath );
    }


    @Override
    public void setKeyStore( final String keyStorePath )
    {
        super.setKeyStore( keyStorePath );
    }


    @Override
    public String getKeyStoreProvider()
    {
        return super.getKeyStoreProvider();
    }


    @Override
    public void setKeyStoreProvider( final String keyStoreProvider )
    {
        super.setKeyStoreProvider( keyStoreProvider );
    }


    @Override
    public String getKeyStoreType()
    {
        return super.getKeyStoreType();
    }


    @Override
    public void setKeyStoreType( final String keyStoreType )
    {
        super.setKeyStoreType( keyStoreType );
    }


    @Override
    public InputStream getKeyStoreInputStream()
    {
        return super.getKeyStoreInputStream();
    }


    @Override
    public void setKeyStoreInputStream( final InputStream keyStoreInputStream )
    {
        super.setKeyStoreInputStream( keyStoreInputStream );
    }


    @Override
    public String getCertAlias()
    {
        return super.getCertAlias();
    }


    @Override
    public void setCertAlias( final String certAlias )
    {
        super.setCertAlias( certAlias );
    }


    @Override
    public String getTrustStore()
    {
        return super.getTrustStore();
    }


    @Override
    public void setTrustStore( final String trustStorePath )
    {
        super.setTrustStore( trustStorePath );
    }


    @Override
    public String getTrustStoreProvider()
    {
        return super.getTrustStoreProvider();
    }


    @Override
    public void setTrustStoreProvider( final String trustStoreProvider )
    {
        super.setTrustStoreProvider( trustStoreProvider );
    }


    @Override
    public String getTrustStoreType()
    {
        return super.getTrustStoreType();
    }


    @Override
    public void setTrustStoreType( final String trustStoreType )
    {
        super.setTrustStoreType( trustStoreType );
    }


    @Override
    public InputStream getTrustStoreInputStream()
    {
        return super.getTrustStoreInputStream();
    }


    @Override
    public void setTrustStoreInputStream( final InputStream trustStoreInputStream )
    {
        super.setTrustStoreInputStream( trustStoreInputStream );
    }


    @Override
    public boolean getNeedClientAuth()
    {
        return super.getNeedClientAuth();
    }


    @Override
    public void setNeedClientAuth( final boolean needClientAuth )
    {
        super.setNeedClientAuth( needClientAuth );
    }


    @Override
    public boolean getWantClientAuth()
    {
        return super.getWantClientAuth();
    }


    @Override
    public void setWantClientAuth( final boolean wantClientAuth )
    {
        super.setWantClientAuth( wantClientAuth );
    }


    @Override
    public boolean getValidateCerts()
    {
        return super.getValidateCerts();
    }


    @Override
    public boolean isValidateCerts()
    {
        return super.isValidateCerts();
    }


    @Override
    public void setValidateCerts( final boolean validateCerts )
    {
        super.setValidateCerts( validateCerts );
    }


    @Override
    public boolean isValidatePeerCerts()
    {
        return super.isValidatePeerCerts();
    }


    @Override
    public void setValidatePeerCerts( final boolean validatePeerCerts )
    {
        super.setValidatePeerCerts( validatePeerCerts );
    }


    @Override
    public boolean isAllowRenegotiate()
    {
        return super.isAllowRenegotiate();
    }


    @Override
    public void setAllowRenegotiate( final boolean allowRenegotiate )
    {
        super.setAllowRenegotiate( allowRenegotiate );
    }


    @Override
    public void setKeyStorePassword( final String password )
    {
        super.setKeyStorePassword( password );
    }


    @Override
    public void setKeyManagerPassword( final String password )
    {
        super.setKeyManagerPassword( password );
    }


    @Override
    public void setTrustStorePassword( final String password )
    {
        super.setTrustStorePassword( password );
    }


    @Override
    public String getProvider()
    {
        return super.getProvider();
    }


    @Override
    public void setProvider( final String provider )
    {
        super.setProvider( provider );
    }


    @Override
    public String getProtocol()
    {
        return super.getProtocol();
    }


    @Override
    public void setProtocol( final String protocol )
    {
        super.setProtocol( protocol );
    }


    @Override
    public String getSecureRandomAlgorithm()
    {
        return super.getSecureRandomAlgorithm();
    }


    @Override
    public void setSecureRandomAlgorithm( final String algorithm )
    {
        super.setSecureRandomAlgorithm( algorithm );
    }


    @Override
    public String getSslKeyManagerFactoryAlgorithm()
    {
        return super.getSslKeyManagerFactoryAlgorithm();
    }


    @Override
    public void setSslKeyManagerFactoryAlgorithm( final String algorithm )
    {
        super.setSslKeyManagerFactoryAlgorithm( algorithm );
    }


    @Override
    public String getTrustManagerFactoryAlgorithm()
    {
        return super.getTrustManagerFactoryAlgorithm();
    }


    @Override
    public boolean isTrustAll()
    {
        return super.isTrustAll();
    }


    @Override
    public void setTrustAll( final boolean trustAll )
    {
        super.setTrustAll( trustAll );
    }


    @Override
    public void setTrustManagerFactoryAlgorithm( final String algorithm )
    {
        super.setTrustManagerFactoryAlgorithm( algorithm );
    }


    @Override
    public String getCrlPath()
    {
        return super.getCrlPath();
    }


    @Override
    public void setCrlPath( final String crlPath )
    {
        super.setCrlPath( crlPath );
    }


    @Override
    public int getMaxCertPathLength()
    {
        return super.getMaxCertPathLength();
    }


    @Override
    public void setMaxCertPathLength( final int maxCertPathLength )
    {
        super.setMaxCertPathLength( maxCertPathLength );
    }


    @Override
    public SSLContext getSslContext()
    {
        return super.getSslContext();
    }


    @Override
    public void setSslContext( final SSLContext sslContext )
    {
        super.setSslContext( sslContext );
    }


    @Override
    protected KeyStore getKeyStore( final InputStream storeStream, final String storePath, final String storeType,
                                    final String storeProvider, final String storePassword ) throws Exception
    {
        return super.getKeyStore( storeStream, storePath, storeType, storeProvider, storePassword );
    }


    @Override
    protected Collection<? extends CRL> loadCRL( final String crlPath ) throws Exception
    {
        return super.loadCRL( crlPath );
    }


    @Override
    protected KeyManager[] getKeyManagers( final KeyStore keyStore ) throws Exception
    {
        return super.getKeyManagers( keyStore );
    }


    @Override
    protected TrustManager[] getTrustManagers( final KeyStore trustStore, final Collection<? extends CRL> crls )
            throws Exception
    {
        return super.getTrustManagers( trustStore, crls );
    }


    @Override
    public void checkKeyStore()
    {
        super.checkKeyStore();
    }


    @Override
    public String[] selectProtocols( final String[] enabledProtocols, final String[] supportedProtocols )
    {
        return super.selectProtocols( enabledProtocols, supportedProtocols );
    }


    @Override
    public String[] selectCipherSuites( final String[] enabledCipherSuites, final String[] supportedCipherSuites )
    {
        return super.selectCipherSuites( enabledCipherSuites, supportedCipherSuites );
    }


    @Override
    protected void checkNotStarted()
    {
        super.checkNotStarted();
    }


    @Override
    public boolean isEnableCRLDP()
    {
        return super.isEnableCRLDP();
    }


    @Override
    public void setEnableCRLDP( final boolean enableCRLDP )
    {
        super.setEnableCRLDP( enableCRLDP );
    }


    @Override
    public boolean isEnableOCSP()
    {
        return super.isEnableOCSP();
    }


    @Override
    public void setEnableOCSP( final boolean enableOCSP )
    {
        super.setEnableOCSP( enableOCSP );
    }


    @Override
    public String getOcspResponderURL()
    {
        return super.getOcspResponderURL();
    }


    @Override
    public void setOcspResponderURL( final String ocspResponderURL )
    {
        super.setOcspResponderURL( ocspResponderURL );
    }


    @Override
    public void setTrustStore( final KeyStore trustStore )
    {
        super.setTrustStore( trustStore );
    }


    @Override
    public void setKeyStoreResource( final Resource resource )
    {
        super.setKeyStoreResource( resource );
    }


    @Override
    public void setTrustStoreResource( final Resource resource )
    {
        super.setTrustStoreResource( resource );
    }


    @Override
    public boolean isSessionCachingEnabled()
    {
        return super.isSessionCachingEnabled();
    }


    @Override
    public void setSessionCachingEnabled( final boolean enableSessionCaching )
    {
        super.setSessionCachingEnabled( enableSessionCaching );
    }


    @Override
    public int getSslSessionCacheSize()
    {
        return super.getSslSessionCacheSize();
    }


    @Override
    public void setSslSessionCacheSize( final int sslSessionCacheSize )
    {
        super.setSslSessionCacheSize( sslSessionCacheSize );
    }


    @Override
    public int getSslSessionTimeout()
    {
        return super.getSslSessionTimeout();
    }


    @Override
    public void setSslSessionTimeout( final int sslSessionTimeout )
    {
        super.setSslSessionTimeout( sslSessionTimeout );
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
        return super.newSslEngine( host, port );
    }


    @Override
    public SSLEngine newSslEngine()
    {
        return super.newSslEngine();
    }


    @Override
    public void customize( final SSLEngine sslEngine )
    {
        super.customize( sslEngine );
    }


    @Override
    public String toString()
    {
        return super.toString();
    }


    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
    }


    @Override
    public boolean isRunning()
    {
        return super.isRunning();
    }


    @Override
    public boolean isStarted()
    {
        return super.isStarted();
    }


    @Override
    public boolean isStarting()
    {
        return super.isStarting();
    }


    @Override
    public boolean isStopping()
    {
        return super.isStopping();
    }


    @Override
    public boolean isStopped()
    {
        return super.isStopped();
    }


    @Override
    public boolean isFailed()
    {
        return super.isFailed();
    }


    @Override
    public void addLifeCycleListener( final Listener listener )
    {
        super.addLifeCycleListener( listener );
    }


    @Override
    public void removeLifeCycleListener( final Listener listener )
    {
        super.removeLifeCycleListener( listener );
    }


    @Override
    public String getState()
    {
        return super.getState();
    }


    @Override
    public int hashCode()
    {
        return super.hashCode();
    }


    @Override
    public boolean equals( final Object obj )
    {
        return super.equals( obj );
    }


    @Override
    protected Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }


    @Override
    protected void finalize() throws Throwable
    {
        super.finalize();
    }
}
