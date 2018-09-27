package io.subutai.core.bazaarmanager.impl.http;


import java.security.KeyStore;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import io.subutai.core.bazaarmanager.api.exception.BazaarManagerException;
import io.subutai.core.bazaarmanager.impl.FingerprintTrustManager;


public class HttpClient
{
    private static final long SECONDS_30 = 30 * 1000L;

    private static final long SECONDS_60 = 60 * 1000L;

    private static final int DEFAULT_MAX_RETRANSMITS = 3;


    private HttpClient()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static WebClient createTrustedWebClientWithAuth( String url, KeyStore keyStore, char[] keyStorePassword,
                                                            byte[] serverFingerprint ) throws BazaarManagerException
    {
        try
        {
            WebClient client = WebClient.create( url );

            // A client certificate is not provided in SSL context if async connection is used.
            // See details: #311 - Registration failure due to inability to find fingerprint.
            Map<String, Object> requestContext = WebClient.getConfig( client ).getRequestContext();
            requestContext.put( "use.async.http.conduit", Boolean.FALSE );

            HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

            HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();

            httpClientPolicy.setConnectionTimeout( SECONDS_30 );

            httpClientPolicy.setReceiveTimeout( SECONDS_60 );

            httpClientPolicy.setMaxRetransmits( DEFAULT_MAX_RETRANSMITS );

            httpConduit.setClient( httpClientPolicy );

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );

            keyManagerFactory.init( keyStore, keyStorePassword );

            TLSClientParameters tlsClientParameters = new TLSClientParameters();

            tlsClientParameters.setDisableCNCheck( true );

            tlsClientParameters
                    .setTrustManagers( new TrustManager[] { new FingerprintTrustManager( serverFingerprint ) } );

            tlsClientParameters.setKeyManagers( keyManagerFactory.getKeyManagers() );

            httpConduit.setTlsClientParameters( tlsClientParameters );

            return client;
        }
        catch ( Exception e )
        {
            throw new BazaarManagerException( e );
        }
    }
}
