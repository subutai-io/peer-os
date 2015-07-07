package io.subutai.pluginmanager.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.tracker.api.Tracker;
import io.subutai.pluginmanager.api.OperationType;
import io.subutai.pluginmanager.api.PluginInfo;
import io.subutai.pluginmanager.api.PluginManager;
import io.subutai.pluginmanager.api.PluginManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


public class PluginManagerImpl implements PluginManager

{
    private static final Logger LOG = LoggerFactory.getLogger( PluginManagerImpl.class.getName() );
    private Commands commands;
    private ManagerHelper managerHelper;
    protected Tracker tracker;
    protected ExecutorService executor;

    public static final String PRODUCT_KEY = "Plugin";


    public PluginManagerImpl( final PeerManager peerManager, final Tracker tracker )
    {
        Preconditions.checkNotNull( peerManager );
        Preconditions.checkNotNull( tracker );

        this.managerHelper = new ManagerHelper( peerManager );
        this.tracker = tracker;
        commands = new Commands();
        executor = Executors.newCachedThreadPool();
    }


    public void destroy()
    {
        executor.shutdown();
    }


    public Tracker getTracker()
    {
        return tracker;
    }


    public Commands getCommands()
    {
        return commands;
    }


    @Override
    public UUID installPlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.INSTALL );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID removePlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.REMOVE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID upgradePlugin( final String pluginName )
    {
        PluginOperationHandler handler =
                new PluginOperationHandler( this, managerHelper, pluginName, OperationType.UPGRADE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public Set<PluginInfo> getInstalledPlugins()
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeCheckIfInstalledCommand() );
        }
        catch ( PluginManagerException e )
        {
            LOG.error( "Error executing plugin installed status", e );
        }

        return managerHelper.parsePluginNamesAndVersions( result );
    }


    @Override
    public Set<PluginInfo> getAvailablePlugins()
    {
        return managerHelper.getDifferenceBetweenPlugins( getInstalledPlugins(), managerHelper.parseJson() );
    }


    @Override
    public Set<String> getAvailablePluginNames()
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeListLocalPluginsCommand() );
            return managerHelper.parseAvailablePluginsNames( result );
        }
        catch ( PluginManagerException e )
        {
            LOG.error( "Error executing command to list installed plugins", e );
            return Sets.newHashSet();
        }
    }


    @Override
    public List<String> getAvaileblePluginVersions()
    {
        return null;
    }


    @Override
    public List<String> getInstalledPluginVersions()
    {

        List<String> versions = new ArrayList<>();
        for ( PluginInfo p : getInstalledPlugins() )
        {
            versions.add( p.getVersion() );
        }
        return versions;
    }


    //    @Override
    //    public List<String> getInstalledPluginNames()
    //    {
    //        List<String> names = new ArrayList<>();
    //        for ( PluginInfo p : getInstalledPlugins() )
    //        {
    //            names.add( p.getPluginName() );
    //        }
    //        return names;
    //    }


    @Override
    public Set<String> getInstalledPluginNames()
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeCheckIfInstalledCommand() );
        }
        catch ( PluginManagerException e )
        {
            LOG.error( "Error executing package status.", e );
        }

        return managerHelper.parsePluginNames( result );
    }


    @Override
    public String getPluginVersion( final String pluginName )
    {
        return null;
    }


    @Override
    public boolean isUpgradeAvailable( final String pluginName )
    {
        boolean upgrade = false;
        String currentVersion = managerHelper.findVersion( getInstalledPlugins(), pluginName );
        String newVersion = managerHelper.findVersion( getAvailablePlugins(), pluginName );

        if ( !currentVersion.equals( newVersion ) )
        {
            upgrade = true;
        }
        return upgrade;
    }


    @Override
    public String getProductKey()
    {
        return PRODUCT_KEY;
    }


    @Override
    public boolean isInstalled( final String p )
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeIsInstalledCommand( p ) );
        }
        catch ( PluginManagerException e )
        {
            return false;
        }
        if ( result.contains( "install ok installed" ) )
        {
            return true;
        }
        return false;
    }


    @Override
    public boolean operationSuccessful( final OperationType operationType )
    {
        boolean result = false;
        switch ( operationType )
        {
            case INSTALL:
                result = PluginOperationHandler.isInstallSuccessful();
                break;
            case REMOVE:
                result = PluginOperationHandler.isRemoveSuccessful();
                break;
        }
        return result;
    }
}
