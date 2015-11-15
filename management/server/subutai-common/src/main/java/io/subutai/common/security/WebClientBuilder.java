package io.subutai.common.security;


import java.security.KeyStore;
import java.util.Arrays;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import io.subutai.common.security.crypto.keystore.KeyStoreData;
import io.subutai.common.security.crypto.keystore.KeyStoreTool;
import io.subutai.common.security.crypto.ssl.SSLManager;
import io.subutai.common.settings.ChannelSettings;
import io.subutai.common.settings.SecuritySettings;


/**
 * Subutai REST client builder
 */
public class WebClientBuilder
{
    private static final Logger LOG = LoggerFactory.getLogger( WebClientBuilder.class );
    public static final long DEFAULT_RECIVE_TIMEOUT = 1000 * 60 * 5;
    public static final long DEFAULT_CONNECTION_TIMEOUT = 1000 * 60;
    public static final int DEFAULT_MAX_RETRANSMITS = 3;
    private static final String PEER_URL_TEMPLATE = "https://%s:%s/rest/v1/peer%s";
    private static final String ENVIRONMENT_URL_TEMPLATE = "https://%s:%s/rest/v1/env%s";


    public static WebClient buildPeerWebClient( final String host, final String path, final Object provider )
    {
        String effectiveUrl = String.format( PEER_URL_TEMPLATE, host, ChannelSettings.SECURE_PORT_X2,
                ( path.startsWith( "/" ) ? path : "/" + path ) );
        WebClient client;
        if ( provider == null )
        {
            client = WebClient.create( effectiveUrl );

        }
        else
        {
            client = WebClient.create( effectiveUrl, Arrays.asList( provider ) );
        }
        client.type( MediaType.APPLICATION_JSON );
        client.accept( MediaType.APPLICATION_JSON );

        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( DEFAULT_CONNECTION_TIMEOUT );
        httpClientPolicy.setReceiveTimeout( DEFAULT_RECIVE_TIMEOUT );
        httpClientPolicy.setMaxRetransmits( DEFAULT_MAX_RETRANSMITS );

        httpConduit.setClient( httpClientPolicy );

        KeyStoreTool keyStoreManager = new KeyStoreTool();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        keyStoreData.setAlias( SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );

        LOG.debug( String.format( "Getting key with alias: %s for url: %s", SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS,
                effectiveUrl ) );

        KeyStoreData trustStoreData = new KeyStoreData();
        trustStoreData.setupTrustStorePx2();
        KeyStore trustStore = keyStoreManager.load( trustStoreData );

        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );

        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientTrustManagers() );
        tlsClientParameters.setKeyManagers( sslManager.getClientKeyManagers() );
        tlsClientParameters.setCertAlias( SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS );
        httpConduit.setTlsClientParameters( tlsClientParameters );
        return client;
    }


    public static WebClient buildEnvironmentWebClient( final String host, final String path, final Object provider )
    {
        String effectiveUrl = String.format( ENVIRONMENT_URL_TEMPLATE, host, ChannelSettings.SECURE_PORT_X2,
                ( path.startsWith( "/" ) ? path : "/" + path ) );
        WebClient client = WebClient.create( effectiveUrl, Arrays.asList( provider ) );
        HTTPConduit httpConduit = ( HTTPConduit ) WebClient.getConfig( client ).getConduit();

        HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
        httpClientPolicy.setConnectionTimeout( DEFAULT_CONNECTION_TIMEOUT );
        httpClientPolicy.setReceiveTimeout( DEFAULT_RECIVE_TIMEOUT );
        httpClientPolicy.setMaxRetransmits( DEFAULT_MAX_RETRANSMITS );

        httpConduit.setClient( httpClientPolicy );

        KeyStoreTool keyStoreManager = new KeyStoreTool();
        KeyStoreData keyStoreData = new KeyStoreData();
        keyStoreData.setupKeyStorePx2();
        keyStoreData.setAlias( SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS );
        KeyStore keyStore = keyStoreManager.load( keyStoreData );

        LOG.debug( String.format( "Getting key with alias: %s for url: %s", SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS,
                effectiveUrl ) );

        KeyStoreData trustStoreData = new KeyStoreData();
        trustStoreData.setupTrustStorePx2();
        KeyStore trustStore = keyStoreManager.load( trustStoreData );

        SSLManager sslManager = new SSLManager( keyStore, keyStoreData, trustStore, trustStoreData );

        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setDisableCNCheck( true );
        tlsClientParameters.setTrustManagers( sslManager.getClientTrustManagers() );
        tlsClientParameters.setKeyManagers( sslManager.getClientKeyManagers() );
        tlsClientParameters.setCertAlias( SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS );
        httpConduit.setTlsClientParameters( tlsClientParameters );
        return client;
    }


    public static WebClient buildPeerWebClient( final String host, final String path )
    {
        return buildPeerWebClient( host, path, null );
    }
}
