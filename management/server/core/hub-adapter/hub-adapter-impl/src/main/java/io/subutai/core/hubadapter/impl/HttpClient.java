package io.subutai.core.hubadapter.impl;


import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import io.subutai.common.security.crypto.ssl.SSLManager;


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


    public static WebClient createTrustedWebClientWithAuth( String url, KeyStore keyStore, char[] keyStorePassword,
                                                            byte[] serverFingerprint, final List<Object> providers )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        WebClient client = WebClient.create( url, providers );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );

        httpConduit.setClient( httpClientPolicy );

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
        keyManagerFactory.init( keyStore, keyStorePassword );


        //        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );


        TrustManager trustManager;
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( new TrustManager[] { new FingerprintTrustManager( serverFingerprint ) } );
        tlsClientParameters.setKeyManagers( keyManagerFactory.getKeyManagers() );
        //        tlsClientParameters.setCertAlias( alias );
        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }


    public static WebClient createTrustedWebClientWithAuth( String url, KeyStore keyStore, char[] keyStorePassword,
                                                            byte[] serverFingerprint )
            throws UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException
    {
        WebClient client = WebClient.create( url );

        // A client certificate is not provided in SSL context if async connection is used.
        // See details: #311 - Registration failure due to inability to find fingerprint.
        Map<String, Object> requestContext = WebClient.getConfig( client ).getRequestContext();
        requestContext.put( "use.async.http.conduit", Boolean.FALSE );

        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );

        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );

        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );

        httpConduit.setClient( httpClientPolicy );

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );

        keyManagerFactory.init( keyStore, keyStorePassword );

        TLSClientParameters tlsClientParameters = new TLSClientParameters();

        tlsClientParameters.setDisableCNCheck( true );

        tlsClientParameters.setTrustManagers( new TrustManager[] { new FingerprintTrustManager( serverFingerprint ) } );

        tlsClientParameters.setKeyManagers( keyManagerFactory.getKeyManagers() );

        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }
}
