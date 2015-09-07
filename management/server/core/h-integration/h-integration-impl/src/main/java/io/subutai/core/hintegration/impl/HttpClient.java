package io.subutai.core.hintegration.impl;


import java.security.KeyStore;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.ssl.SSLManager;


/**
 * Created by ermek on 9/6/15.
 */
public class HttpClient
{
    private static long defaultReceiveTimeout = 1000 * 60 * 5;
    private static long defaultConnectionTimeout = 1000 * 60;
    private static int defaultMaxRetransmits = 3;

    public static WebClient createTrustedWebClient( String url )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );


        httpConduit.setClient( httpClientPolicy );

        SSLManager sslManager = new SSLManager( null, null, null, null );

        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientFullTrustManagers() );
        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }

    public static WebClient createTrustedWebClientWithAuth( String url, String alias )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );

        httpConduit.setClient( httpClientPolicy );

        KeyStoreTool keyStoreManager = new KeyStoreTool();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        keyStoreData.setAlias( alias );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );

//        LOG.debug( String.format( "Getting keyStore with alias: %s for url: %s", alias, url ) );
//        LOG.debug( String.format( "KeyStore: %s", keyStore.toString() ) );

        KeyStoreData trustStoreData = new KeyStoreData();
        trustStoreData.setupTrustStorePx2();
        KeyStore trustStore = keyStoreManager.load( trustStoreData );

        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );

        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientTrustManagers() );
        tlsClientParameters.setKeyManagers( sslManager.getClientKeyManagers() );
        tlsClientParameters.setCertAlias( alias );
        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }



}
