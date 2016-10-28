package io.subutai.common.util;


import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import io.subutai.common.security.crypto.ssl.SSLManager;
import io.subutai.common.settings.Common;


public class RestUtil
{
    private static long defaultReceiveTimeout = Common.DEFAULT_RECEIVE_TIMEOUT;
    private static long defaultConnectionTimeout = Common.DEFAULT_CONNECTION_TIMEOUT;
    private static int defaultMaxRetransmits = Common.DEFAULT_MAX_RETRANSMITS;


    public static WebClient createWebClient( String url, long connectTimeout, long receiveTimeout, int maxRetries )
    {
        WebClient client = WebClient.create( url );

        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( connectTimeout );
        httpClientPolicy.setReceiveTimeout( receiveTimeout );
        httpClientPolicy.setMaxRetransmits( maxRetries );

        httpConduit.setClient( httpClientPolicy );
        return client;
    }


    public static void close( Response response, WebClient webClient )
    {
        close( response );
        close( webClient );
    }


    public static void close( WebClient webClient )
    {
        if ( webClient != null )
        {
            try
            {
                webClient.close();
            }
            catch ( Exception ignore )
            {
                //ignore
            }
        }
    }


    public static void close( Response response )
    {
        if ( response != null )
        {
            try
            {
                response.close();
            }
            catch ( Exception ignore )
            {
                //ignore
            }
        }
    }


    public static WebClient createWebClient( String url )
    {
        return createWebClient( url, defaultConnectionTimeout, defaultReceiveTimeout, defaultMaxRetransmits );
    }


    public WebClient getTrustedWebClient( String url, Object provider )
    {
        return createTrustedWebClient( url, provider );
    }


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


    public static WebClient createTrustedWebClient( String url, Object provider )
    {
        WebClient client = WebClient.create( url, Arrays.asList( provider ) );

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
}
