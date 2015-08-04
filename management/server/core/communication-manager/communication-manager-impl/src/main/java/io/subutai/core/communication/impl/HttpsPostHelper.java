package io.subutai.core.communication.impl;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;

import io.subutai.core.communication.api.PGPKeyNotFound;
import io.subutai.core.communication.api.Response;
import io.subutai.core.communication.api.SecurityMaterials;


public class HttpsPostHelper
{
    private static final Logger log = LoggerFactory.getLogger( HttpsPostHelper.class );

    private String uri;

    private HttpClient httpClient;
    private boolean devMode = false;
    private SecurityMaterials securityMaterials;


    public HttpsPostHelper( String uri, SecurityMaterials securityMaterials, boolean devMode ) throws Exception
    {
        this.uri = uri;
        this.devMode = devMode;
        this.securityMaterials = securityMaterials;

        httpClient = initHttpClient( securityMaterials.getKeyStore(), securityMaterials.getKeyStorePassword(),
                securityMaterials.getTrustStrategy() );
    }


    private HttpClient initHttpClient( KeyStore keyStore, char[] password, TrustStrategy trustStrategy )
            throws Exception
    {
        SSLContext sslContext =
                SSLContexts.custom().loadKeyMaterial( keyStore, password ).loadTrustMaterial( trustStrategy ).build();

        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory( sslContext, NoopHostnameVerifier.INSTANCE );

        HttpClient httpClient = HttpClients.custom().setSSLSocketFactory( sslSocketFactory )
                                           .setRetryHandler( new DefaultHttpRequestRetryHandler( 0, false ) ).build();

        return httpClient;
    }


    public Response execute( String encData ) throws IOException, PGPKeyNotFound, PGPException
    {
        HttpPost post = new HttpPost( uri );

        post.setEntity( getHttpEntity( encData ) );

        HttpResponse response = httpClient.execute( post );

        return new Response( response.getStatusLine().getStatusCode(), readContent( response ) );
    }


    private HttpEntity getHttpEntity( String envelope )
            throws UnsupportedEncodingException, PGPKeyNotFound, PGPException
    {
        List<NameValuePair> params = new ArrayList<>();

        params.add( new BasicNameValuePair( "envelope", envelope ) );

        if ( devMode )
        {
            params.add( new BasicNameValuePair( "fingerprint",
                    Hex.encodeHexString( securityMaterials.getRecipientGPGPublicKey().getFingerprint() ) ) );
        }

        return new UrlEncodedFormEntity( params );
    }


    private String readContent( HttpResponse response ) throws IOException
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( response.getEntity().getContent() ) );
        StringBuilder builder = new StringBuilder();

        String line;

        while ( ( line = reader.readLine() ) != null )
        {
            builder.append( line ).append( '\n' );
        }

        return builder.toString();
    }
}
