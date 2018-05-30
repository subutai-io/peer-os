package io.subutai.core.template.impl;


import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
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
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.protocol.Template;
import io.subutai.common.settings.Common;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.template.api.TemplateManager;


public class TemplateManagerImpl implements TemplateManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateManagerImpl.class.getName() );
    private static final String GORJUN_LIST_TEMPLATES_URL = Common.KURJUN_BASE_URL + "/template/list?token=%s";
    private static final String GORJUN_LIST_PRIVATE_TEMPLATES_URL =
            Common.KURJUN_BASE_URL + "/template/list?owner=%s&token=%s";
    private static final String GORJUN_GET_VERIFIED_TEMPLATE_URL =
            Common.KURJUN_BASE_URL + "/template/info?name=%s&verified=true";

    private static final int TEMPLATE_CACHE_TTL_SEC = 60;
    private static final int HIT_CACHE_IF_ERROR_INTERVAL_SEC = 30;
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


    WebClient getWebClient( String url )
    {
        return RestUtil.createWebClient( url, 3000, 5000, 1 );
    }


    @Override
    public void resetTemplateCache()
    {
        lastTemplatesFetchTime = 0L;
    }


    @Override
    public Set<Template> getTemplates()
    {
        String kurjunToken = null;

        Session session = identityManager.getActiveSession();

        if ( session != null )
        {
            kurjunToken = session.getCdnToken();
        }

        return getTemplates( kurjunToken );
    }


    private boolean needToUpdateCache()
    {
        return templatesCache.isEmpty() || System.currentTimeMillis() - lastTemplatesFetchTime >= TimeUnit.SECONDS
                .toMillis( TEMPLATE_CACHE_TTL_SEC )
                && System.currentTimeMillis() - lastTemplatesFetchErrorTime > TimeUnit.SECONDS
                .toMillis( HIT_CACHE_IF_ERROR_INTERVAL_SEC );
    }


    @Override
    public Set<Template> getTemplates( String kurjunToken )
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


            WebClient webClient = null;
            Response response = null;

            try
            {
                webClient = getWebClient(
                        String.format( GORJUN_LIST_TEMPLATES_URL, kurjunToken == null ? "" : kurjunToken ) );

                response = webClient.get();

                Set<Template> freshTemplateList = Sets.newHashSet();

                String json = response.readEntity( String.class ).trim();

                if ( json.startsWith( "[" ) )
                {
                    Set<Template> templates = JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                    {
                    }.getType() );

                    freshTemplateList.addAll( templates );
                }
                else
                {
                    freshTemplateList.add( JsonUtil.fromJson( json, Template.class ) );
                }


                if ( !CollectionUtil.isCollectionEmpty( freshTemplateList ) )
                {
                    lastTemplatesFetchTime = System.currentTimeMillis();

                    templatesCache.clear();

                    for ( Template template : freshTemplateList )
                    {
                        if ( template != null )
                        {
                            templatesCache.add( template );
                        }
                    }
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting templates from Kurjun", e );

                lastTemplatesFetchErrorTime = System.currentTimeMillis();
            }
            finally
            {
                RestUtil.close( response, webClient );
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
    public Template getTemplate( final String id, final String kurjunToken )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( id ) );

        for ( Template template : getTemplates( kurjunToken ) )
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
    public List<Template> getTemplatesByOwner( final String token )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( token ), "Invalid token" );

        String owner = getOwner( token );

        Preconditions.checkNotNull( owner, "Owner not found" );

        List<Template> templates = Lists.newArrayList();

        for ( Template template : getTemplates() )
        {
            //TODO template must have one owner
            if ( !CollectionUtil.isCollectionEmpty( template.getOwners() ) && template.getOwners()
                                                                                      .contains( owner.toLowerCase() ) )
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

        try
        {
            return getVerifiedTemplatesCache().get( templateName );
        }
        catch ( Exception e )
        {
            LOG.error( "Error getting verified template by name {} from Kurjun: {}", templateName, e.getMessage() );

            return null;
        }
    }


    @Override
    public List<Template> getUserPrivateTemplates()
    {
        List<Template> templates = Lists.newArrayList();

        Session session = identityManager.getActiveSession();

        if ( session != null )
        {
            String kurjunToken = session.getCdnToken();

            if ( kurjunToken != null )
            {
                WebClient webClient = null;
                Response response = null;

                try
                {
                    webClient = getWebClient( String.format( GORJUN_LIST_PRIVATE_TEMPLATES_URL,
                            session.getUser().getFingerprint().toLowerCase(), kurjunToken ) );

                    response = webClient.get();

                    if ( response.getStatus() != Response.Status.OK.getStatusCode() )
                    {
                        return templates;
                    }

                    String json = response.readEntity( String.class ).trim();

                    if ( json.startsWith( "[" ) )
                    {
                        Set<Template> privateTemplates = JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                        {
                        }.getType() );

                        for ( Template template : privateTemplates )
                        {
                            if ( template != null )
                            {
                                templates.add( template );
                            }
                        }
                    }
                    else
                    {
                        Template template = JsonUtil.fromJson( json, Template.class );

                        if ( template != null )
                        {
                            templates.add( template );
                        }
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error getting private templates from Kurjun", e );
                }
                finally
                {
                    RestUtil.close( response, webClient );
                }
            }
        }

        return templates;
    }


    private LoadingCache<String, Template> verifiedTemplatesCache =
            CacheBuilder.newBuilder().maximumSize( 1000 ).expireAfterWrite( TEMPLATE_CACHE_TTL_SEC, TimeUnit.SECONDS )
                        .build( new CacheLoader<String, Template>()
                        {
                            @Override
                            public Template load( String templateName ) throws Exception
                            {
                                WebClient webClient = null;
                                Response response = null;

                                try
                                {
                                    webClient = getWebClient( String.format( GORJUN_GET_VERIFIED_TEMPLATE_URL,
                                            templateName.toLowerCase() ) );

                                    response = webClient.get();

                                    String json = response.readEntity( String.class ).trim();

                                    Template template;
                                    if ( json.startsWith( "[" ) )
                                    {
                                        Set<Template> templates =
                                                JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                                                {
                                                }.getType() );

                                        template = templates.iterator().next();
                                    }
                                    else
                                    {
                                        template = JsonUtil.fromJson( json, Template.class );
                                    }

                                    return template == null || StringUtils.isBlank( template.getId() ) ? null :
                                           template;
                                }
                                finally
                                {
                                    RestUtil.close( response, webClient );
                                }
                            }
                        } );


    LoadingCache<String, Template> getVerifiedTemplatesCache()
    {
        return verifiedTemplatesCache;
    }

    //Bazaar CDN


    private CloseableHttpClient getHttpsClient()
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


    public String obtainCdnToken( final String signedFingerprint )
    {
        Preconditions.checkNotNull( signedFingerprint );

        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpPost post = new HttpPost( String.format( "https://%s/rest/v1/cdn/token", Common.HUB_IP ) );

            List<NameValuePair> form = Lists.newArrayList();
            form.add( new BasicNameValuePair( "signedFingerprint", signedFingerprint ) );
            UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity( form, Consts.UTF_8 );
            post.setEntity( urlEncodedFormEntity );
            CloseableHttpResponse response = client.execute( post );

            try
            {
                HttpEntity entity = response.getEntity();
                String content = IOUtils.toString( entity.getContent() );
                EntityUtils.consume( entity );

                if ( response.getStatusLine().getStatusCode() == 200 )
                {
                    identityManager.getActiveSession().setCdnToken( content );

                    return content;
                }
                else
                {
                    LOG.error( "Http code: " + response.getStatusLine().getStatusCode() + " Msg: " + content );
                }
            }
            finally
            {
                IOUtils.closeQuietly( response );
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
                    new HttpGet( String.format( "https://%s/rest/v1/cdn/users/%s", Common.HUB_IP, getFingerprint() ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                return response.getStatusLine().getStatusCode() == 200;
            }
            finally
            {
                IOUtils.closeQuietly( response );
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
        CloseableHttpClient client = getHttpsClient();
        try
        {
            HttpGet httpGet = new HttpGet(
                    String.format( "https://%s/rest/v1/cdn/users/username?token=%s", Common.HUB_IP, token ) );
            CloseableHttpResponse response = client.execute( httpGet );
            try
            {
                if ( response.getStatusLine().getStatusCode() == 200 )
                {
                    HttpEntity entity = response.getEntity();
                    String content = IOUtils.toString( entity.getContent() );
                    EntityUtils.consume( entity );
                    return content;
                }
            }
            finally
            {
                IOUtils.closeQuietly( response );
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
