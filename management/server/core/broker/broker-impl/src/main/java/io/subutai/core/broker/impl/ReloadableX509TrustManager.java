package io.subutai.core.broker.impl;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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
        X509Certificate[] issuers = trustManager.getAcceptedIssuers();
        return issuers;
    }


    public void reloadTrustManager() throws Exception
    {

        // load keystore from specified cert store (or default)
        KeyStore ts = KeyStore.getInstance( KeyStore.getDefaultType() );
        InputStream in = new FileInputStream( trustStorePath );
        try
        {
            ts.load( in, null );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
        finally
        {
            in.close();
        }

        // initialize a new TMF with the ts we just loaded
        TrustManagerFactory tmf = TrustManagerFactory.getInstance( TrustManagerFactory.getDefaultAlgorithm() );
        tmf.init( ts );

        // acquire X509 trust manager from factory
        TrustManager tms[] = tmf.getTrustManagers();
        for ( int i = 0; i < tms.length; i++ )
        {
            if ( tms[i] instanceof X509TrustManager )
            {
                trustManager = ( X509TrustManager ) tms[i];
                return;
            }
        }
        throw new NoSuchAlgorithmException( "No X509TrustManager in TrustManagerFactory" );
    }


    protected synchronized void addServerCertAndReload( String alias, X509Certificate cert ) throws Exception
    {

        // import the cert into file trust store
        File tsfile = new File( this.trustStorePath );
        java.io.FileInputStream fis;
        if ( tsfile.exists() )
        {
            Files.copy( Paths.get( this.trustStorePath ),
                    Paths.get( String.format( "%s.backup_%d", this.trustStorePath, System.currentTimeMillis() ) ),
                    StandardCopyOption.REPLACE_EXISTING );
            fis = new FileInputStream( tsfile );
        }
        else
        {
            throw new Exception( "Truststore " + tsfile.getAbsolutePath() + " does not exist!" );
        }

        KeyStore ts = KeyStore.getInstance( KeyStore.getDefaultType() );

        char[] keystorePass = this.tspassword.toCharArray();
        try
        {
            ts.load( fis, keystorePass );
        }
        finally
        {
            fis.close();
        }

        ts.setCertificateEntry( alias, cert );

        ts.store( new FileOutputStream( this.trustStorePath ), keystorePass );

        reloadTrustManager();
    }
}
