package io.subutai.core.hintegration.impl;


import java.security.KeyStore;

import javax.net.ssl.SSLContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;

import io.subutai.hub.common.dto.EnvironmentDTO;


public class HIntegrationImpl
{
    private static final Logger LOG = LoggerFactory.getLogger( HIntegrationImpl.class.getName() );


    /**
     * @param keyStore - keystore with peer X509 certificate with CN=fingerprint of peer pub key
     * @param keyStorePassword - keystore password
     */
    private static HttpClient initHttpClient( KeyStore keyStore, String keyStorePassword ) throws Exception
    {

        SSLContext sslContext = SSLContexts.custom().loadKeyMaterial( keyStore, keyStorePassword.toCharArray() )
                                           .loadTrustMaterial( new TrustSelfSignedStrategy() ).build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory( sslContext, NoopHostnameVerifier.INSTANCE );

        return HttpClients.custom().setSSLSocketFactory( sslSocketFactory )
                          .setRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) ).build();
    }


    public void init()
    {
        LOG.debug( "H-INTEGRATION" );

        new TrustSelfSignedStrategy();

        EnvironmentDTO environmentDTO;

        LOG.debug( "DTO" + EnvironmentDTO.class.toString() );
    }
}
