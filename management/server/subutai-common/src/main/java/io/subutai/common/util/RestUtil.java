package io.subutai.common.util;


import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

import io.subutai.common.exception.HTTPException;
import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.ssl.SSLManager;
import io.subutai.common.settings.ChannelSettings;

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




    public WebClient createTrustedWebClientWithAuthAndProviders( final String url, final String alias,
                                                     final List<Object> providers )
    {
        WebClient client = WebClient.create( url, providers );
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

        LOG.debug( String.format( "Getting keyStore with alias: %s for url: %s", alias, url ) );
        LOG.debug( String.format( "KeyStore: %s", keyStore.toString() ) );

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


    public static enum RequestType
    {
        GET, DELETE, POST
    }


    public RestUtil()
    {
    }


    public RestUtil( final long defaultReceiveTimeout, final long defaultConnectionTimeout, final int maxRetransmits )
    {
        Preconditions.checkArgument( defaultReceiveTimeout > 0, "Receive timeout must be greater than 0" );
        Preconditions.checkArgument( defaultConnectionTimeout > 0, "Connection timeout must be greater than 0" );

        setDefaultReceiveTimeout( defaultReceiveTimeout );
        setDefaultConnectionTimeout( defaultConnectionTimeout );
        setDefaultMaxRetransmits( maxRetransmits );
    }


    public String request( RequestType requestType, String url, String alias, Map<String, String> params,
                           Map<String, String> headers ) throws HTTPException
    {
        Preconditions.checkNotNull( requestType, "Invalid request type" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( url ), "Invalid url" );
        Response response = null;
        try
        {
            response = executeClientAndGetResponse( url, requestType, alias, params, headers );
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
                    //ignore
                    LOG.warn( "Error closing response object", ignore );
                }
            }
        }
        return null;
    }


    private Response executeClientAndGetResponse( final String url, final RequestType requestType, final String alias,
                                                  final Map<String, String> params, final Map<String, String> headers )
            throws MalformedURLException, HTTPException
    {
        WebClient client = null;

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
                    LOG.debug( String.format( "Request type: %s, %s", requestType, url ) );
                    client = createTrustedWebClientWithAuth( url, alias );
                    break;
                default:
                    client = createWebClient( url );
                    break;
            }
            Form form = new Form();
            constructClientParams( params, requestType, form, client, headers );
            switch ( requestType )
            {
                case GET:
                    return client.get();
                case POST:
                    return client.form( form );
                case DELETE:
                    return client.delete();
                default:
                    throw new HTTPException( String.format( "Unrecognized requestType: %s", requestType.name() ) );
            }
        }
        finally
        {
            if ( client != null )
            {
                try
                {
                    client.close();
                }
                catch ( Exception ignore )
                {
                    //ignore
                    LOG.warn( "Error disposing web client", ignore );
                }
            }
        }
    }


    private void constructClientParams( final Map<String, String> params, final RequestType requestType,
                                        final Form form, final WebClient client, final Map<String, String> headers )
    {
        if ( params != null )
        {
            for ( Map.Entry<String, String> entry : params.entrySet() )
            {
                if ( requestType == RequestType.POST )
                {
                    form.set( entry.getKey(), entry.getValue() );
                }
                else
                {
                    client.query( entry.getKey(), entry.getValue() );
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


    public WebClient getTrustedWebClient( String url )
    {
        return createTrustedWebClient( url );
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

        LOG.debug( String.format( "Getting keyStore with alias: %s for url: %s", alias, url ) );
        LOG.debug( String.format( "KeyStore: %s", keyStore.toString() ) );

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


    private static synchronized void setDefaultReceiveTimeout( final long timeout )
    {
        defaultReceiveTimeout = timeout;
    }


    private static synchronized void setDefaultConnectionTimeout( final long timeout )
    {
        defaultConnectionTimeout = timeout;
    }


    private static synchronized void setDefaultMaxRetransmits( final int timeout )
    {
        defaultMaxRetransmits = timeout;
    }
}
