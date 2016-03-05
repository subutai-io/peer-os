package io.subutai.core.kurjun.impl;


import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.OptionalBinder;

import ai.subut.kurjun.model.repository.RemoteRepository;
import ai.subut.kurjun.repo.util.http.WebClientFactory;
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
    protected void configure()
    {
        OptionalBinder<WebClientFactory> opb = OptionalBinder.newOptionalBinder( binder(), WebClientFactory.class );
        opb.setBinding().to( TrustedWebClientFactoryModule.class );
    }


}

