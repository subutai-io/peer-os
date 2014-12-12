package org.safehaus.subutai.wol.impl.manager;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.wol.api.PluginInfo;
import org.safehaus.subutai.wol.api.PluginManager;
import org.safehaus.subutai.wol.api.PluginManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerImpl implements PluginManager

{
    private static final Logger LOG = LoggerFactory.getLogger( PluginManagerImpl.class.getName() );
    private final PeerManager peerManager;
    private Commands commands;
    private ManagerHelper managerHelper;


    public PluginManagerImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
        commands = new Commands();
        this.managerHelper = new ManagerHelper( peerManager );
    }


    @Override
    public void installPlugin( final String packageName )
    {

    }


    @Override
    public void removePlugin( final String packageName )
    {

    }


    @Override
    public void upgradePlugin( final String packageName )
    {

    }


    @Override
    public List<PluginInfo> getInstalledPlugins()
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

        List<PluginInfo> plugins = managerHelper.parsePluginNamesAndVersions( result );
        return plugins;

    }


    @Override
    public List<PluginInfo> getAvailablePlugins()
    {
        return null;
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
            versions.add( p.getPackageVersion() );
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
        return true;
    }
}
