package io.subutai.core.bazaarmanager.impl;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;


public class FingerprintTrustManager implements X509TrustManager
{
    public FingerprintTrustManager( final byte[] serverFingerprint )
    {
    }


    @Override
    public void checkClientTrusted( final X509Certificate[] x509Certificates, final String s )
            throws CertificateException
    {

    }


    @Override
    public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        X509Certificate cert = chain[0];

        try
        {
            cert.checkValidity();
        }
        catch ( Exception e )
        {
            throw new CertificateException( e.toString() );
        }
    }


    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        return new X509Certificate[0];
    }
}