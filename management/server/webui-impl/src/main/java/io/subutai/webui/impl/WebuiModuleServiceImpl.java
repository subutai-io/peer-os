package io.subutai.webui.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

import io.subutai.webui.api.WebuiModule;
import io.subutai.webui.api.WebuiModuleService;


public class WebuiModuleServiceImpl implements WebuiModuleService
{
    private static final Logger LOG = LoggerFactory.getLogger( WebuiModuleServiceImpl.class.getName() );
    private Set<WebuiModule> modules = new HashSet<WebuiModule>();

    private BundleContext bcontext;
    private String subutaiAppJs;


    public void setBcontext( BundleContext bcontext )
    {
        this.bcontext = bcontext;
    }


    public void init()
    {
        try
        {
            Bundle bundle = bcontext.getBundle();
            InputStream is = bundle.getEntry( "/subutai-app.js" ).openStream();
            subutaiAppJs = readFile( is );
        }
        catch ( IOException e )
        {
            LOG.error( "The file subutai-app.js not found", e );
        }
    }


    public synchronized void registerModule( WebuiModule module )
    {
        if ( module != null )
        {
            modules.add( module );
        }
        else
        {
            LOG.info( "Register module invoked." );
        }
    }


    public synchronized void unregisterModule( WebuiModule module )
    {
        if ( module != null )
        {
            modules.remove( module );
        }
    }


    @Override
    public String getModulesListJson()
    {
        if ( modules.isEmpty() )
        {
            return "{}";
        }

        StringBuilder result = new StringBuilder( "[" );

        for ( Iterator<WebuiModule> iterator = modules.iterator(); iterator.hasNext(); )
        {
            final WebuiModule module = iterator.next();

            if ( result.indexOf( module.getName() ) == -1 )
            {
                result.append( module.getModuleInfo() ).append( "," );
            }
        }

        if ( result.length() > 1 )
        {
            result.deleteCharAt( result.length() - 1 );
        }


        return result.append( "]" ).toString();
    }


    @Override
    public String getModulesListLazyLoadConfig()
    {
        StringBuilder builder = new StringBuilder();
        for ( WebuiModule module : modules )
        {
            try
            {
                String moduleInfo = module.getAngularDependecyList();

                if ( !Strings.isNullOrEmpty( moduleInfo ) && !isPluginPresent( module, builder ) )
                {
                    //add only if a plugin with such a name is not already present
                    builder.append( moduleInfo ).append( "\n" );
                }
            }
            catch ( Exception e )
            {
                LOG.warn( "Error getting angular dependency", e );
            }
        }

        return subutaiAppJs.replace( ".state()", builder.toString() );
    }


    private boolean isPluginPresent( final WebuiModule module, final StringBuilder builder )
    {
        return builder.indexOf( String.format( ".state('%s'", module.getName().toLowerCase() ) ) != -1;
    }


    private String readFile( InputStream is ) throws IOException
    {
        java.util.Scanner s = new java.util.Scanner( is ).useDelimiter( "\\A" );
        return s.hasNext() ? s.next() : "";
    }
}
