package io.subutai.webui.impl;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.webui.api.WebuiModule;
import io.subutai.webui.api.WebuiModuleService;


public class WebuiModuleServiceImpl implements WebuiModuleService
{
    private static final Logger LOG = LoggerFactory.getLogger( WebuiModuleServiceImpl.class.getName() );
    private Set<WebuiModule> modules =  new HashSet<WebuiModule>(  );

    private BundleContext bcontext;
    private String subutaiAppJs;

    public void setBcontext(BundleContext bcontext ) {
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
        if( modules.size() == 0 )
        {
            return "{}";
        }

        return "[" + modules.stream().map( m -> m.getModuleInfo() ).reduce( (m1, m2) -> m1 + "," + m2 ).get() + ']';
    }


    @Override
    public String getModulesListLazyLoadConfig()
    {
        StringBuilder builder = new StringBuilder(  );
        for( WebuiModule module : modules )
        {
            builder.append( module.getAngularDependecyList() + "\n" );
        }

        return subutaiAppJs.replace( ".state()", builder.toString() );
    }

    private String readFile( InputStream is ) throws IOException
    {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
