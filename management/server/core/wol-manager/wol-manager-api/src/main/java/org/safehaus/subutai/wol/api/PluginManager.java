package org.safehaus.subutai.wol.api;


import java.util.List;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginManager
{
    public void installPlugin( String packageName );

    public void removePlugin( String packageName );

    public void upgradePlugin( String packageName );

    public List<PluginInfo> getInstalledPlugins();

    public List<PluginInfo> getAvailablePlugins();

    public List<String> getAvailablePluginNames();

    public List<String> getAvaileblePluginVersions();

    public List<String> getInstalledPluginVersions();

    public List<String> getInstalledPluginNames();

    public String getPluginVersion( String pluginName );

    public boolean isUpgradeAvailable( String pluginName);
}
