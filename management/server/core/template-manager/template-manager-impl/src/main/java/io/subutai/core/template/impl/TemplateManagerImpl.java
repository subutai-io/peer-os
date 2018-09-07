package io.subutai.core.template.impl;


import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.Consts;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.template.api.TemplateManager;


public class TemplateManagerImpl implements TemplateManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateManagerImpl.class.getName() );

    private static final int TEMPLATE_CACHE_TTL_SEC = 60;
    private static final int HIT_CACHE_IF_ERROR_INTERVAL_SEC = 30;
    private static final String[] VERIFIED_OWNERS = { "subutai", "jenkins" };
    private Set<Template> templatesCache = Sets.newConcurrentHashSet();
    private volatile long lastTemplatesFetchTime = 0L;
    private volatile long lastTemplatesFetchErrorTime = 0L;
    private final ReentrantLock lock = new ReentrantLock( true );

    private final IdentityManager identityManager;


    public TemplateManagerImpl( final IdentityManager identityManager )
    {
        Preconditions.checkNotNull( identityManager );

        this.identityManager = identityManager;
    }


    @Override
    public void resetTemplateCache()
    {
        lastTemplatesFetchTime = 0L;
    }


    private boolean needToUpdateCache()
    {
        return templatesCache.isEmpty() || System.currentTimeMillis() - lastTemplatesFetchTime >= TimeUnit.SECONDS
                .toMillis( TEMPLATE_CACHE_TTL_SEC )
                && System.currentTimeMillis() - lastTemplatesFetchErrorTime > TimeUnit.SECONDS
                .toMillis( HIT_CACHE_IF_ERROR_INTERVAL_SEC );
    }


    @Override
    public Set<Template> getTemplates()
    {

        if ( !needToUpdateCache() )
        {
            return templatesCache;
        }

        lock.lock();

        try
        {
            //check again just in case
            if ( !needToUpdateCache() )
            {
                return templatesCache;
            }


            CloseableHttpClient client = getHttpsClient();
            try
            {
                HttpGet httpGet = new HttpGet(
                        String.format( "https://%s/rest/v1/cdn/templates?version=latest", Common.BAZAAR_IP ) );
                CloseableHttpResponse response = client.execute( httpGet );

                try
                {
                    Set<Template> freshTemplateList = Sets.newHashSet();

                    String json = readContent( response );

                    if ( !Strings.isNullOrEmpty( json ) )
                    {
                        Set<Template> templates = JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                        {
                        }.getType() );

                        freshTemplateList.addAll( templates );
                    }


                    if ( !CollectionUtil.isCollectionEmpty( freshTemplateList ) )
                    {
                        lastTemplatesFetchTime = System.currentTimeMillis();

                        templatesCache.clear();

                        for ( Template template : freshTemplateList )
                        {
                            if ( template != null && !Strings.isNullOrEmpty( template.getId() ) )
                            {
                                templatesCache.add( template );
                            }
                        }
                    }
                }
                finally
                {
                    close( response );
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting templates from CDN", e );

                lastTemplatesFetchErrorTime = System.currentTimeMillis();
            }
            finally
            {
                IOUtils.closeQuietly( client );
            }
        }
        finally
        {
            lock.unlock();
        }

        return templatesCache;
    }


    @Override
    public Template getTemplate( final String id )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ) );

        for ( Template template : getTemplates() )
        {
            if ( template.getId().equalsIgnoreCase( id ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public Template getTemplateByName( final String name )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        Template verifiedTemplate = getVerifiedTemplateByName( name );

        if ( verifiedTemplate != null )
        {
            return verifiedTemplate;
        }

        for ( Template template : getTemplates() )
        {
            if ( name.equalsIgnoreCase( template.getName() ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public List<Template> getTemplatesByOwner( final String owner )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( owner ), "Invalid owner" );

        List<Template> templates = Lists.newArrayList();

        for ( Template template : getTemplates() )
        {
            if ( template.getOwner().equalsIgnoreCase( owner ) )
            {
                templates.add( template );
            }
        }

        return templates;
    }


    @Override
    public Template getVerifiedTemplateByName( final String templateName )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( templateName ) );

        List<Template> verifiedTemplates = Lists.newArrayList();

        for ( String owner : VERIFIED_OWNERS )
        {
            verifiedTemplates.addAll( getTemplatesByOwner( owner ) );
        }

        for ( Template template : verifiedTemplates )
        {
            if ( template.getName().equalsIgnoreCase( templateName ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public List<Template> getOwnTemplates()
    {
        String cdnToken = null;

        if ( identityManager.getActiveSession() != null )
        {
            cdnToken = identityManager.getActiveSession().getCdnToken();
        }

        if ( cdnToken != null )
        {
            String owner = getOwner( cdnToken );

            if ( owner != null )
            {
                return getTemplatesByOwner( owner );
            }
        }

        return Lists.newArrayList();
    }


    //Bazaar CDN


    protected CloseableHttpClient getHttpsClient()
    {
        try
        {
            RequestConfig config = RequestConfig.custom().setSocketTimeout( 5000 ).setConnectTimeout( 5000 ).build();

            SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.loadTrustMaterial( null, ( TrustStrategy ) ( x509Certificates, s ) -> true );
            SSLConnectionSocketFactory sslSocketFactory =
                    new SSLConnectionSocketFactory( sslContextBuilder.build(), NoopHostnameVerifier.INSTANCE );

            return HttpClients.custom().setDefaultRequestConfig( config ).setSSLSocketFactory( sslSocketFactory )
                              .build();
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }

        return HttpClients.createDefault();
    }


    protected String readContent( CloseableHttpResponse response )
    {
        try
        {
            return EntityUtils.toString( response.getEntity() );
        }
        catch ( Exception e )
        {
            LOG.error( "Error reading entity content", e );

            return null;
        }
    }


    private void close( CloseableHttpResponse response )
    {
        EntityUtils.consumeQuietly( response.getEntity() );

        IOUtils.closeQuietly( response );
    }


    public String getFingerprint()
    {
        User user = identityManager.getActiveUser();
        return user.getFingerprint().toLowerCase();
    }


    public String getObtainedCdnToken()
    {
        String token = null;

        if ( identityManager.getActiveSession() != null )
        {
            token = identityManager.getActiveSession().getCdnToken();
        }

        return token;
    }


    public String getTokenRequest()
    {
        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpGet httpGet = new HttpGet(
                    String.format( "https://%s/rest/v1/cdn/token?fingerprint=%s", Common.BAZAAR_IP, getFingerprint() ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                String content = readContent( response );

                if ( response.getStatusLine().getStatusCode() == 201 )
                {
                    return content;
                }
                else
                {
                    LOG.error( "Failed to obtain token request: " + response.getStatusLine() + ", " + content );
                }
            }
            finally
            {
                close( response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return null;
    }


    public String obtainCdnToken( final String signedRequest )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( signedRequest ) );

        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpPost post = new HttpPost( String.format( "https://%s/rest/v1/cdn/token", Common.BAZAAR_IP ) );

            List<NameValuePair> form = Lists.newArrayList();
            form.add( new BasicNameValuePair( "request", signedRequest ) );
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
            post.setEntity( urlEncodedFormEntity );
            CloseableHttpResponse response = client.execute( post );

            try
            {
                String content = readContent( response );

                if ( response.getStatusLine().getStatusCode() == 201 )
                {
                    identityManager.getActiveSession().setCdnToken( content );

                    resetTemplateCache();

                    return content;
                }
                else
                {
                    LOG.error( "Failed to obtain token: " + response.getStatusLine() + ", " + content );
                }
            }
            finally
            {
                close( response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return null;
    }


    public boolean isRegisteredWithCdn()
    {
        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpGet httpGet =
                    new HttpGet( String.format( "https://%s/rest/v1/cdn/users/%s", Common.BAZAAR_IP, getFingerprint() ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                return response.getStatusLine().getStatusCode() == 200;
            }
            finally
            {
                close( response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return false;
    }


    public String getOwner( String token )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( token ) );

        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpGet httpGet = new HttpGet(
                    String.format( "https://%s/rest/v1/cdn/users/username?token=%s", Common.BAZAAR_IP, token ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                if ( response.getStatusLine().getStatusCode() == 200 )
                {
                    return readContent( response );
                }
            }
            finally
            {
                close( response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            IOUtils.closeQuietly( client );
        }

        return null;
    }
}
