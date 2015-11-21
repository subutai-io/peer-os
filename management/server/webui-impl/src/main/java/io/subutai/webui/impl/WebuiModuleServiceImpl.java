package io.subutai.webui.impl;


import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        else
        {
            return modules.stream().map( m -> m.getModuleInfo() ).reduce( (m1, m2) -> m1 + "," + m2 ).get();
        }
    }


    @Override
    public String getModulesListLazyLoadConfig()
    {
        return null;
    }
}
