package io.subutai.core.broker.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


public class ReloadableX509TrustManager implements X509TrustManager
{

    private final String trustStorePath;
    private final String tspassword;
    private X509TrustManager trustManager;


    public ReloadableX509TrustManager( String tspath, String tspassword ) throws Exception
    {
        this.trustStorePath = tspath;
        this.tspassword = tspassword;
        reloadTrustManager();
    }


    @Override
    public void checkClientTrusted( X509Certificate[] chain, String authType ) throws CertificateException
    {
        try
        {
            trustManager.checkClientTrusted( chain, authType );
        }
        catch ( Exception e )
        {
            try
            {
                reloadTrustManager();
            }
            catch ( Exception ex )
            {
                throw new CertificateException( ex );
            }
            trustManager.checkClientTrusted( chain, authType );
        }
    }


    @Override
    public void checkServerTrusted( X509Certificate[] chain, String authType ) throws CertificateException
    {
        try
        {
            trustManager.checkServerTrusted( chain, authType );
        }
        catch ( CertificateException cx )
        {
            try
            {
                reloadTrustManager();
            }
            catch ( Exception e )
            {
                throw new CertificateException( e );
            }
            trustManager.checkServerTrusted( chain, authType );
        }
    }


    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return trustManager.getAcceptedIssuers();
    }


    public void reloadTrustManager() throws Exception
    {
        // load keystore from specified cert store (or default)
        KeyStore ts = KeyStore.getInstance( KeyStore.getDefaultType() );

        File tsFile = new File( trustStorePath );

        if ( tsFile.exists() )
        {
            char[] keystorePass = this.tspassword.toCharArray();

            try ( FileInputStream fis = new FileInputStream( tsFile ) )
            {
                ts.load( fis, keystorePass );
            }
        }
        else
        {
            ts.load( null, null );
        }

        // initialize a new TMF with the ts we just loaded
        TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
        tmf.init( ts );

        // acquire X509 trust manager from factory
        TrustManager tms[] = tmf.getTrustManagers();
        for ( final TrustManager tm : tms )
        {
            if ( tm instanceof X509TrustManager )
            {
                trustManager = ( X509TrustManager ) tm;
                return;
            }
        }
        throw new NoSuchAlgorithmException( "No X509TrustManager in TrustManagerFactory" );
    }


    protected synchronized void addServerCertAndReload( String alias, X509Certificate cert ) throws Exception
    {

        // import the cert into file trust store
        File tsFile = new File( this.trustStorePath );

        char[] keystorePass = this.tspassword.toCharArray();

        KeyStore ts = KeyStore.getInstance( KeyStore.getDefaultType() );

        if ( tsFile.exists() )
        {
            Files.copy( Paths.get( this.trustStorePath ),
                    Paths.get( String.format( "%s.backup_%d", this.trustStorePath, System.currentTimeMillis() ) ),
                    StandardCopyOption.REPLACE_EXISTING );

            try ( FileInputStream fis = new FileInputStream( tsFile ) )
            {
                ts.load( fis, keystorePass );
            }
        }
        else
        {
            ts.load( null, null );
        }

        ts.setCertificateEntry( alias, cert );

        ts.store( new FileOutputStream( this.trustStorePath ), keystorePass );

        reloadTrustManager();
    }
}
