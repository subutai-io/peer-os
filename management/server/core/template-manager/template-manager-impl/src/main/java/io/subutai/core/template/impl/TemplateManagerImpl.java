package io.subutai.core.template.impl;


import java.util.Set;

import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.cxf.jaxrs.client.WebClient;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.subutai.common.protocol.Template;
import io.subutai.common.util.CollectionUtil;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.RestUtil;
import io.subutai.core.template.api.TemplateManager;


public class TemplateManagerImpl implements TemplateManager
{
    private static final Logger LOG = LoggerFactory.getLogger( TemplateManagerImpl.class.getName() );
    private static final String GORJUN_LIST_TEMPLATES_URL = "http://localhost:8338/kurjun/rest/template/list";
    private static final String GORJUN_GET_VERIFIED_TEMPLATE_URL =
            "http://localhost:8338/kurjun/rest/template/info?name=%s&verified=true";

    private static final int TEMPLATE_CACHE_TTL_SEC = 30;
    private Set<Template> templatesCache = Sets.newHashSet();
    private long lastTemplatesFetchTime;


    WebClient getWebClient( String url )
    {
        return RestUtil.createWebClient( url, 3000, 5000, 1 );
    }


    @Override
    public Set<Template> getTemplates()
    {

        if ( templatesCache.isEmpty()
                || ( System.currentTimeMillis() - lastTemplatesFetchTime ) / 1000 >= TEMPLATE_CACHE_TTL_SEC )
        {
            WebClient webClient = null;
            Response response = null;

            try
            {
                webClient = getWebClient( GORJUN_LIST_TEMPLATES_URL );

                response = webClient.get();

                Set<Template> freshTemplateList =
                        JsonUtil.fromJson( response.readEntity( String.class ), new TypeToken<Set<Template>>()
                        {
                        }.getType() );

                lastTemplatesFetchTime = System.currentTimeMillis();

                if ( !CollectionUtil.isCollectionEmpty( freshTemplateList ) )
                {
                    templatesCache = freshTemplateList;
                }
            }
            catch ( Exception e )
            {
                LOG.error( "Error getting templates from local Gorjun", e );
            }
            finally
            {
                RestUtil.close( response, webClient );
            }
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
            if ( template.getName().equalsIgnoreCase( name ) )
            {
                return template;
            }
        }

        return null;
    }


    @Override
    public Template getVerifiedTemplateByName( final String name )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( name ) );

        WebClient webClient = null;
        Response response = null;

        try
        {
            webClient = getWebClient( String.format( GORJUN_GET_VERIFIED_TEMPLATE_URL, name.toLowerCase() ) );

            response = webClient.get();

            return JsonUtil.fromJson( response.readEntity( String.class ), Template.class );
        }
        catch ( Exception e )
        {
            LOG.error( String.format( "Error getting verified template by name %s from local Gorjun", name ), e );
        }
        finally
        {
            RestUtil.close( response, webClient );
        }

        return null;
    }
}
