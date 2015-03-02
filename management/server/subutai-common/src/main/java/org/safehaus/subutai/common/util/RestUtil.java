package org.safehaus.subutai.common.util;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.Map;

import javax.ws.rs.core.Response;

import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreData;
import org.safehaus.subutai.common.security.crypto.keystore.KeyStoreManager;
import org.safehaus.subutai.common.security.crypto.ssl.SSLManager;
import org.safehaus.subutai.common.settings.ChannelSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.ext.form.Form;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


public class RestUtil
{
    private static final Logger LOG = LoggerFactory.getLogger( RestUtil.class );
    private static long defaultReceiveTimeout = 1000 * 60 * 5;
    private static long defaultConnectionTimeout = 1000 * 60;
    private static int defaultMaxRetransmits = 3;


    public static enum RequestType
    {
        GET, POST
    }


    public RestUtil()
    {
    }


    public RestUtil( final long defaultReceiveTimeout, final long defaultConnectionTimeout, final int maxRetransmits )
    {
        Preconditions.checkArgument( defaultReceiveTimeout > 0, "Receive timeout must be greater than 0" );
        Preconditions.checkArgument( defaultConnectionTimeout > 0, "Connection timeout must be greater than 0" );

        RestUtil.defaultReceiveTimeout = defaultReceiveTimeout;
        RestUtil.defaultConnectionTimeout = defaultConnectionTimeout;
        RestUtil.defaultMaxRetransmits = maxRetransmits;
    }


    public String request( RequestType requestType, String url, Map<String, String> params,
                           Map<String, String> headers ) throws HTTPException
    {

        Preconditions.checkNotNull( requestType, "Invalid request type" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( url ), "Invalid url" );

        WebClient client = null;
        Response response = null;
        try
        {
            URL urlObject = new URL( url );
            String port = String.valueOf( urlObject.getPort() );
            switch ( port )
            {
                case ChannelSettings.SECURE_PORT_X1:
                    client = createTrustedWebClient( url );
                    break;
                case ChannelSettings.SECURE_PORT_X2:
                    client = createTrustedWebClientWithAuth( url );
                    break;
                case ChannelSettings.SECURE_PORT_X3:
                    client = createTrustedWebClientWithEnvAuth( url, "environment certificate alias" );
                    break;
                default:
                    client = createWebClient( url );
                    break;
            }
            Form form = new Form();
            if ( params != null )
            {
                for ( Map.Entry<String, String> entry : params.entrySet() )
                {
                    if ( requestType == RequestType.GET )
                    {
                        client.query( entry.getKey(), entry.getValue() );
                    }
                    else
                    {
                        form.set( entry.getKey(), entry.getValue() );
                    }
                }
            }
            if ( headers != null )
            {
                for ( Map.Entry<String, String> entry : headers.entrySet() )
                {
                    client.header( entry.getKey(), entry.getValue() );
                }
            }
            response = requestType == RequestType.GET ? client.get() : client.form( form );
            if ( !NumUtil.isIntBetween( response.getStatus(), 200, 299 ) )
            {
                if ( response.hasEntity() )
                {
                    throw new HTTPException( response.readEntity( String.class ) );
                }
                else
                {
                    throw new HTTPException( String.format( "Http status code: %d", response.getStatus() ) );
                }
            }
            else if ( response.hasEntity() )
            {
                return response.readEntity( String.class );
            }
        }
        catch ( MalformedURLException e )
        {
            LOG.error( "Error in url path.", e );
        }
        finally
        {
            if ( response != null )
            {
                try
                {
                    response.close();
                }
                catch ( Exception ignore )
                {
                }
            }
            if ( client != null )
            {
                try
                {
                    client.close();
                }
                catch ( Exception ignore )
                {
                }
            }
        }

        return null;
    }


    public static WebClient createWebClient( String url )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );

        httpConduit.setClient( httpClientPolicy );
        return client;
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


    public static WebClient createTrustedWebClientWithAuth( String url )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );


        httpConduit.setClient( httpClientPolicy );

        KeyStoreManager keyStoreManager = new KeyStoreManager();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        KeyStore keyStore = keyStoreManager.load( keyStoreData );

        KeyStoreData trustStoreData = new KeyStoreData();
        trustStoreData.setupTrustStorePx2();
        KeyStore trustStore = keyStoreManager.load( trustStoreData );

        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );


        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientTrustManagers() );
        tlsClientParameters.setKeyManagers( sslManager.getClientKeyManagers() );
        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }

    public static WebClient createTrustedWebClientWithEnvAuth( String url, String environmentAlias )
    {
        WebClient client = WebClient.create( url );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( defaultConnectionTimeout );
        httpClientPolicy.setReceiveTimeout( defaultReceiveTimeout );
        httpClientPolicy.setMaxRetransmits( defaultMaxRetransmits );


        httpConduit.setClient( httpClientPolicy );

        KeyStoreManager keyStoreManager = new KeyStoreManager();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        keyStoreData.setAlias( environmentAlias );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );

        KeyStoreData trustStoreData = new KeyStoreData();
        trustStoreData.setupTrustStorePx2();
        KeyStore trustStore = keyStoreManager.load( trustStoreData );

        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );


        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientTrustManagers() );
        tlsClientParameters.setKeyManagers( sslManager.getClientKeyManagers() );
        httpConduit.setTlsClientParameters( tlsClientParameters );

        return client;
    }
}
