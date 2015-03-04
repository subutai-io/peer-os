package org.safehaus.subutai.core.identity.ssl;


import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by talas on 3/3/15.
 */
public class CustomTrustManager implements X509TrustManager
{

    private static final Logger log = LoggerFactory.getLogger( CustomTrustManager.class );


    @Override
    public void checkClientTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        log.warn( "##############   checkClientTrusted" );
    }


    @Override
    public void checkServerTrusted( final X509Certificate[] chain, final String authType ) throws CertificateException
    {
        log.warn( "#################   checkServerTrusted" );
    }


    @Override
    public X509Certificate[] getAcceptedIssuers()
    {
        log.warn( "##################   checkServerTrusted" );
        return new X509Certificate[0];
    }
}
