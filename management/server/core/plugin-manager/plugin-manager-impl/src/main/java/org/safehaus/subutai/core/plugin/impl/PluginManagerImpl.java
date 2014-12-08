package org.safehaus.subutai.core.plugin.impl;


import java.util.ArrayList;
import java.util.List;

import org.safehaus.subutai.core.plugin.api.PluginInfo;
import org.safehaus.subutai.core.plugin.api.PluginManager;


/**
 * Created by ebru on 08.12.2014.
 */
public class PluginManagerImpl implements PluginManager
{
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
    public String getPluginVersion( final String pluginName )
    {
        return null;
    }
}
