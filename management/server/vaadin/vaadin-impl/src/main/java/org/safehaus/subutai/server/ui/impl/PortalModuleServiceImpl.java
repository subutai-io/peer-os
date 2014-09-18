package org.safehaus.subutai.server.ui.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;


public class PortalModuleServiceImpl implements PortalModuleService {

    private List<PortalModule> modules = Collections.synchronizedList( new ArrayList<PortalModule>() );

    private List<PortalModuleListener> listeners =
            Collections.synchronizedList( new ArrayList<PortalModuleListener>() );


    public void registerModule( PortalModule module ) {
        if ( module != null )
        {
            System.out.println( "ModuleServiceImpl: Registering module " + module.getId() );
            modules.add( module );
            for ( PortalModuleListener listener : listeners )
            {
                listener.moduleRegistered( module );
            }
        }
    }


    public void unregisterModule( PortalModule module ) {
        if ( module != null )
        {
            System.out.println( "ModuleServiceImpl: Unregister module " + module.getId() );
            modules.remove( module );
            for ( PortalModuleListener listener : listeners )
            {
                listener.moduleUnregistered( module );
            }
        }
    }


    @Override
    public PortalModule getModule( String pModuleId ) {
        for ( PortalModule module : modules )
        {
            if ( pModuleId.equals( module.getId() ) )
            {
                return module;
            }
        }
        throw new IllegalArgumentException( "Cannot find any module with the id given" );
    }


    public List<PortalModule> getModules() {
        List<PortalModule> pluginModules = Collections.synchronizedList(new ArrayList<PortalModule>());
        for (PortalModule module : modules) {
            if (module.isCorePlugin() == null || !module.isCorePlugin()) {
                pluginModules.add(module);
            }
        }
        return Collections.unmodifiableList(pluginModules);
    }

    public List<PortalModule> getCoreModules() {
        List<PortalModule> coreModules = Collections.synchronizedList(new ArrayList<PortalModule>());
        for (PortalModule module : modules) {
//            LOG.log(Level.WARNING, module.getId());
            if (module.isCorePlugin() != null && module.isCorePlugin()) {
                coreModules.add(module);
            }
        }
        return Collections.unmodifiableList(coreModules);
    }

    public synchronized void addListener( PortalModuleListener listener ) {
        System.out.println( "ModuleServiceImpl: Adding listener " + listener );
        listeners.add( listener );
    }


    public synchronized void removeListener( PortalModuleListener listener ) {
        if ( listener != null )
        {
            System.out.println( "ModuleServiceImpl: Removing listener " + listener );
            listeners.remove( listener );
        }
    }
}