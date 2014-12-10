package org.safehaus.subutai.core.plugin.api;


import java.util.List;


/**
 * Created by ebru on 08.12.2014.
 */
public interface PluginManager
{
    public void installPlugin(String packageName);

    public void removePlugin(String packageName);

    public void upgradePlugin( String packageName);

    public List<PluginInfo> getInstalledPlugins();

    public List<String> getPluginNames();

    public String getPluginVersion( String pluginName);
}
