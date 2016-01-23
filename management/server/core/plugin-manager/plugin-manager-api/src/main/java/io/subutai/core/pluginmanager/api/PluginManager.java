package io.subutai.core.pluginmanager.api;


import java.util.ArrayList;
import java.util.List;

import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;


public interface PluginManager
{
    public void register( String name, String version, String pathToKar, ArrayList<PermissionJson> permissions );

    public ConfigDataService getConfigDataService();

    public List<PluginDetails> getInstalledPlugins();

    public void unregister( Long pluginId );

    public void setPermissions( Long pluginId, String permissionJson );
}
