package io.subutai.core.pluginmanager.api;


import java.util.List;

import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;


public interface PluginManager
{
    void register( String name, String version, String pathToKar, List<PermissionJson> permissions );

    ConfigDataService getConfigDataService();

    List<PluginDetails> getInstalledPlugins();

    void unregister( Long pluginId );

    void setPermissions( Long pluginId, String permissionJson );
}
