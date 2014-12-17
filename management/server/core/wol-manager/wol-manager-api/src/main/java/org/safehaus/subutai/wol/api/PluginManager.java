package org.safehaus.subutai.wol.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginManager
{
    public UUID installPlugin( String pluginName );

    public UUID removePlugin( String pluginName );

    public UUID upgradePlugin( String pluginName );

    public Set<PluginInfo> getInstalledPlugins();

    public Set<PluginInfo> getAvailablePlugins();

    public List<String> getAvailablePluginNames();

    public List<String> getAvaileblePluginVersions();

    public List<String> getInstalledPluginVersions();

    public List<String> getInstalledPluginNames();

    public String getPluginVersion( String pluginName );

    public boolean isUpgradeAvailable( String pluginName);

    public String getProductKey();
}
