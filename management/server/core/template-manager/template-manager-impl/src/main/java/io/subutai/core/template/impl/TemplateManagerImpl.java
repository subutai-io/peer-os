package io.subutai.core.template.impl;


import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

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
import io.subutai.core.template.api.TemplateManager;


public class TemplateManagerImpl implements TemplateManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateManagerImpl.class.getName() );
    private static final String GORJUN_LIST_TEMPLATES_URL = Common.LOCAL_KURJUN_BASE_URL + "/template/info?token=%s";
    private static final String GORJUN_LIST_PRIVATE_TEMPLATES_URL =
            Common.LOCAL_KURJUN_BASE_URL + "/template/info?owner=%s&token=%s";
    private static final String GORJUN_GET_VERIFIED_TEMPLATE_URL =
            Common.LOCAL_KURJUN_BASE_URL + "/template/info?name=%s&verified=true";
    private static final int TEMPLATE_CACHE_TTL_SEC = 30;
    private static final int HIT_CACHE_IF_ERROR_INTERVAL_SEC = 30;
    private Set<Template> templatesCache = Sets.newHashSet();
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
            kurjunToken = session.getKurjunToken();
        }

        return getTemplates( kurjunToken );
    }


    @Override
    public Set<Template> getTemplates( String kurjunToken )
    {
        boolean needToUpdate = System.currentTimeMillis() - lastTemplatesFetchTime >= TimeUnit.SECONDS
                .toMillis( TEMPLATE_CACHE_TTL_SEC )
                && System.currentTimeMillis() - lastTemplatesFetchErrorTime > TimeUnit.SECONDS
                .toMillis( HIT_CACHE_IF_ERROR_INTERVAL_SEC );

        if ( !needToUpdate )
        {
            return templatesCache;
        }

        lock.lock();

        try
        {
            //check again just in case
            needToUpdate = System.currentTimeMillis() - lastTemplatesFetchTime >= TimeUnit.SECONDS
                    .toMillis( TEMPLATE_CACHE_TTL_SEC )
                    && System.currentTimeMillis() - lastTemplatesFetchErrorTime > TimeUnit.SECONDS
                    .toMillis( HIT_CACHE_IF_ERROR_INTERVAL_SEC );

            if ( !needToUpdate )
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

                    templatesCache = freshTemplateList;
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting templates from local Gorjun", e );

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
            if ( template.getName().equalsIgnoreCase( name ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public List<Template> getTemplatesByOwner( final String owner )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( owner ) );

        List<Template> templates = Lists.newArrayList();

        for ( Template template : getTemplates() )
        {
            if ( template.getOwners().contains( owner.toLowerCase() ) )
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
            LOG.error( "Error getting verified template by name {} from local Gorjun: {}", templateName,
                    e.getMessage() );

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
            String kurjunToken = session.getKurjunToken();

            if ( kurjunToken != null )
            {
                WebClient webClient = null;
                Response response = null;

                try
                {
                    webClient = getWebClient( String.format( GORJUN_LIST_PRIVATE_TEMPLATES_URL,
                            session.getUser().getFingerprint().toLowerCase(), kurjunToken ) );

                    response = webClient.get();

                    String json = response.readEntity( String.class ).trim();

                    if ( json.startsWith( "[" ) )
                    {
                        Set<Template> privateTemplates = JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                        {
                        }.getType() );

                        templates.addAll( privateTemplates );
                    }
                    else
                    {
                        templates.add( JsonUtil.fromJson( json, Template.class ) );
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error getting private templates from local Gorjun", e );
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

                                    if ( json.startsWith( "[" ) )
                                    {
                                        Set<Template> templates =
                                                JsonUtil.fromJson( json, new TypeToken<Set<Template>>()
                                                {
                                                }.getType() );

                                        return templates.iterator().next();
                                    }
                                    else
                                    {
                                        return JsonUtil.fromJson( json, Template.class );
                                    }
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
}
