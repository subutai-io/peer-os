package io.subutai.core.kurjun.impl;


import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.OptionalBinder;

import ai.subut.kurjun.model.repository.RemoteRepository;
import ai.subut.kurjun.repo.util.http.WebClientFactory;
import io.subutai.common.security.crypto.ssl.NaiveTrustManager;
import io.subutai.common.settings.Common;
import io.subutai.common.settings.SecuritySettings;
import io.subutai.common.util.RestUtil;


/**
 * Guice module that re-binds web client factory so that Subutai's trusted web client instances are used.
 *
 */
public class TrustedWebClientFactoryModule extends AbstractModule implements WebClientFactory
{


    @Override
    public WebClient make( RemoteRepository remoteRepository, String path, Map<String, String> queryParams )
    {
        try
        {
            URL url = WebClientFactory.buildUrl( remoteRepository, path, queryParams );
            String alias = SecuritySettings.KEYSTORE_PX2_ROOT_ALIAS;

            return RestUtil.createTrustedWebClient( url.toString(), alias );
        }
        catch ( URISyntaxException | MalformedURLException ex )
        {
            throw new IllegalArgumentException( ex );
        }
    }


    @Override
    public WebClient makeSecure( final RemoteRepository remoteRepository, final String s,
                                 final Map<String, String> map )
    {
        try
        {
            URL url = WebClientFactory.buildUrl( remoteRepository, s, map );

            WebClient webClient = RestUtil.createTrustedWebClient( url.toExternalForm() );

            return webClient;
        }
        catch ( URISyntaxException | MalformedURLException e )
        {
            e.printStackTrace();
        }

        return null;
    }


    @Override
    public URLConnection openSecureConnection( final RemoteRepository remoteRepository, final String path, final Map<String, String> queryParams )
    {
        try
        {
            URL url = WebClientFactory.buildUrl( remoteRepository, path, queryParams);
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout( ( int ) Common.DEFAULT_CONNECTION_TIMEOUT );
            conn.setReadTimeout( ( int ) Common.DEFAULT_RECEIVE_TIMEOUT );
            if ( conn instanceof HttpsURLConnection )
            {
                SSLContext sc = SSLContext.getInstance("TLS");
                sc.init(null, new TrustManager[] { new NaiveTrustManager() },
                        new java.security.SecureRandom());
                (( HttpsURLConnection )conn).setSSLSocketFactory(sc.getSocketFactory());
            }
            conn.connect();
            return conn;
        }
        catch ( URISyntaxException | NoSuchAlgorithmException | KeyManagementException | IOException e )
        {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    protected void configure()
    {
        OptionalBinder<WebClientFactory> opb = OptionalBinder.newOptionalBinder( binder(), WebClientFactory.class );
        opb.setBinding().to( TrustedWebClientFactoryModule.class );
    }


}

