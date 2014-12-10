package org.safehaus.subutai.core.plugin.impl;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.plugin.api.PluginInfo;
import org.safehaus.subutai.core.plugin.api.PluginManager;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerImpl implements PluginManager

{
    private final PeerManager peerManager;


    public PluginManagerImpl( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
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
        PluginInfo hadoop = new PluginInfoImpl();
        hadoop.setPackageName("hadoop-subutai-plugin");
        hadoop.setPluginName( "hadoop" );
        hadoop.setPackageVersion( "2.0.0" );


        List<PluginInfo> plugins = new ArrayList<>();
        plugins.add( hadoop );

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
    public List<String> getInstalledPluginNames()
    {
        List<String> names = new ArrayList<>();
        for( PluginInfo p : getInstalledPlugins() )
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
}
