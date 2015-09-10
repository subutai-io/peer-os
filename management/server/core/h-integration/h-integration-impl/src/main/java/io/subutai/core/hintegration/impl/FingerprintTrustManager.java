package io.subutai.core.hintegration.impl;


import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;


/**
 * Created by tzhamakeev on 9/7/15.
 */
public class FingerprintTrustManager implements X509TrustManager
{
    private static final Logger LOG = LoggerFactory.getLogger( FingerprintTrustManager.class );

    private byte[] serverFingerprint;


    public FingerprintTrustManager( final byte[] serverFingerprint )
    {
        this.serverFingerprint = serverFingerprint;
    }


    @Override
    public void checkClientTrusted( final X509Certificate[] x509Certificates, final String s )
            throws CertificateException
    {

    }


    @Override
    public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        LOG.debug( String.format( "Chain length: %d. AuthType: %s", chain.length, authType ) );

        if ( chain.length != 1 )
        {
            throw new CertificateException( "Chain not supported." );
        }

        X509Certificate cert = chain[0];
        LOG.debug( cert.toString() );

        try
        {
            cert.checkValidity();
            Principal p = cert.getSubjectDN();
            String fingerPrint = StringUtils.substringBetween( p.getName(), "CN=", "," );
            LOG.debug( "Fingerprint: " + fingerPrint );

            if ( !Arrays.equals( serverFingerprint, Hex.decodeHex( fingerPrint.toCharArray() ) ) )
            {
                throw new CertificateException( "Invalid fingerprint" );
            }
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