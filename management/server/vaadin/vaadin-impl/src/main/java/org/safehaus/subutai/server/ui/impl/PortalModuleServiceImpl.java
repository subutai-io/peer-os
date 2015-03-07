package org.safehaus.subutai.server.ui.impl;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.naming.NamingException;

import org.safehaus.subutai.common.util.ServiceLocator;
import org.safehaus.subutai.core.identity.api.IdentityManager;
import org.safehaus.subutai.server.ui.api.PortalModule;
import org.safehaus.subutai.server.ui.api.PortalModuleListener;
import org.safehaus.subutai.server.ui.api.PortalModuleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;


public class PortalModuleServiceImpl implements PortalModuleService
{

    private static final Logger LOG = LoggerFactory.getLogger( PortalModuleServiceImpl.class.getName() );
    private List<PortalModule> modules = Collections.synchronizedList( new ArrayList<PortalModule>() );

    private List<PortalModuleListener> listeners =
            Collections.synchronizedList( new ArrayList<PortalModuleListener>() );

    private ServiceLocator serviceLocator = new ServiceLocator();


    public synchronized void registerModule( PortalModule module )
    {
        if ( module != null )
        {
            //TODO place to filter out modules

            try
            {
                IdentityManager identityManager = serviceLocator.getService( IdentityManager.class );
                identityManager.updateUserPortalModule(
                        identityManager.createMockUserPortalModule( module.getId(), module.getName() ) );
            }
            catch ( NamingException e )
            {
                LOG.error( "Error accessing identityManager via serviceLocator", e );
            }

            LOG.info( String.format( "Registering module: %s ", module.getId() ) );
            modules.add( module );
            for ( PortalModuleListener listener : listeners )
            {
                listener.moduleRegistered( module );
            }
        }
        else
        {
            LOG.info( "Register module invoked." );
        }
    }


    public synchronized void unregisterModule( PortalModule module )
    {
        if ( module != null )
        {
            LOG.info( "Unregister module " + module.getId() );
            modules.remove( module );
            for ( PortalModuleListener listener : listeners )
            {
                listener.moduleUnregistered( module );
            }
        }
    }


    @Override
    public PortalModule getModule( String pModuleId )
    {
        if ( !Strings.isNullOrEmpty( pModuleId ) )
        {
            for ( PortalModule module : modules )
            {
                if ( pModuleId.equals( module.getId() ) )
                {
                    return module;
                }
            }
        }
        throw new IllegalArgumentException( "Cannot find any module with the id given" );
    }


    public List<PortalModule> getModules()
    {
        List<PortalModule> pluginModules = Collections.synchronizedList( new ArrayList<PortalModule>() );
        for ( PortalModule module : modules )
        {
            if ( module.isCorePlugin() == null || !module.isCorePlugin() )
            {
                pluginModules.add( module );
            }
        }
        return Collections.unmodifiableList( pluginModules );
    }


    public List<PortalModule> getCoreModules()
    {
        List<PortalModule> coreModules = Collections.synchronizedList( new ArrayList<PortalModule>() );
        for ( PortalModule module : modules )
        {
            //            LOG.log(Level.WARNING, module.getId());
            if ( module.isCorePlugin() != null && module.isCorePlugin() )
            {
                coreModules.add( module );
            }
        }
        return Collections.unmodifiableList( coreModules );
    }


    public synchronized void addListener( PortalModuleListener listener )
    {
        if ( listener != null )
        {
            LOG.info( "Adding listener " + listener );
            listeners.add( listener );
        }
    }


    public synchronized void removeListener( PortalModuleListener listener )
    {
        if ( listener != null )
        {
            LOG.info( "Removing listener " + listener );
            listeners.remove( listener );
        }
    }


    @Override
    public void loadDependentModule( final String moduleId )
    {
        for ( final PortalModuleListener listener : listeners )
        {
            listener.loadDependentModule( moduleId );
        }
    }
}