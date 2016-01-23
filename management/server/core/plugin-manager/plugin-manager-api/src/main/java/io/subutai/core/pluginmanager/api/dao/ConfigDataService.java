package io.subutai.core.pluginmanager.api.dao;


import java.util.List;

import io.subutai.core.pluginmanager.api.model.PluginDetails;


public interface ConfigDataService
{
    public void saveDetails( String name, String version, String pathToKar/*, Long userId, Long roleId, String token */);

    public List<PluginDetails> getInstalledPlugins();

    public void deleteDetails( Long pluginId );

    public PluginDetails getPluginDetails( Long pluginId );
}
