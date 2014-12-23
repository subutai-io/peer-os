package org.safehaus.subutai.wol.impl.manager;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManager;
import org.safehaus.subutai.wol.api.PluginManagerException;
import org.safehaus.subutai.wol.impl.manager.handler.OperationType;
import org.safehaus.subutai.wol.impl.manager.handler.PluginOperationHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.safehaus.subutai.core.tracker.api.Tracker;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerImpl implements PluginManager

{
    private static final Logger LOG = LoggerFactory.getLogger( PluginManagerImpl.class.getName() );
    private final PeerManager peerManager;
    private Commands commands;
    private ManagerHelper managerHelper;
    protected Tracker tracker;
    protected ExecutorService executor;

    public static final String PRODUCT_KEY = "Plugin";


    public PluginManagerImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
        commands = new Commands();
        this.managerHelper = new ManagerHelper( peerManager );
        executor = Executors.newCachedThreadPool();
    }

    public Tracker getTracker()
    {
        return tracker;
    }

    public Commands getCommands()
    {
        return commands;
    }

    public void setTracker( final Tracker tracker )
    {
        this.tracker = tracker;
    }


    @Override
    public UUID installPlugin( final String pluginName )
    {
        PluginOperationHandler handler = new PluginOperationHandler( this, managerHelper, pluginName, OperationType.INSTALL );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID removePlugin( final String pluginName )
    {
        PluginOperationHandler handler = new PluginOperationHandler( this, managerHelper, pluginName, OperationType.REMOVE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public UUID upgradePlugin( final String pluginName )
    {
        PluginOperationHandler handler = new PluginOperationHandler( this, managerHelper, pluginName, OperationType.UPGRADE );
        executor.execute( handler );
        return handler.getTrackerId();
    }


    @Override
    public Set<PluginInfo> getInstalledPlugins()
    {
        String result = null;
        try
        {
            result = managerHelper.execute( commands.makeCheckCommand() );


        }
        catch ( PluginManagerException e )
        {
            LOG.error( e.getMessage() );
            e.printStackTrace();
        }

        Set<PluginInfo> plugins = managerHelper.parsePluginNamesAndVersions( result );
        return plugins;

    }


    @Override
    public Set<PluginInfo> getAvailablePlugins()
    {
        return managerHelper.getDifferenceBetweenPlugins( getInstalledPlugins(), managerHelper.parseJson());
    }


    @Override
    public List<String> getAvailablePluginNames()
    {
        return null;
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
        for( PluginInfo p : getInstalledPlugins() )
        {
            versions.add( p.getVersion() );
        }
        return versions;
    }


    @Override
    public List<String> getInstalledPluginNames()
    {
        List<String> names = new ArrayList<>();
        for ( PluginInfo p : getInstalledPlugins() )
        {
            names.add( p.getPluginName() );
        }
        return names;
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

        if( !currentVersion.equals( newVersion ) )
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
}
