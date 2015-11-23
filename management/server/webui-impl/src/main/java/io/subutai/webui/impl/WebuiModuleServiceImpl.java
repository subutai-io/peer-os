package io.subutai.webui.impl;


import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.util.JsonUtil;
import io.subutai.webui.api.WebuiModule;
import io.subutai.webui.api.WebuiModuleService;


public class WebuiModuleServiceImpl implements WebuiModuleService
{
    private static final Logger LOG = LoggerFactory.getLogger( WebuiModuleServiceImpl.class.getName() );
    private Set<WebuiModule> modules =  new HashSet<WebuiModule>(  );


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
        if( modules.size() == 0 )
        {
            return "";
        }

        String buffer;
        StringBuilder states = new StringBuilder( );
        try
        {
            buffer = Files.readAllLines( Paths.get( this.getClass().getResource( "/subutai-app.js" ).toURI() ) ).toString();

            for( WebuiModule module : modules )
            {
                WebuiModuleResourse webuiModuleResourse = JsonUtil.fromJson( module.getModuleInfo(), WebuiModuleResourse.class);

                states.append(
                        String.format( ".state( '%s', %s )",
                                webuiModuleResourse.getName().toLowerCase(),
                                module.getAngularDependecyList()));
            }

            return buffer.replace( ".state()", states.toString() );
        }
        catch ( IOException | NullPointerException | URISyntaxException e )
        {
            LOG.error( "Error reading file subutai-app", e );
            return "";
        }
    }
}
