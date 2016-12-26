package io.subutai.core.pluginmanager.api;


import java.io.IOException;
import java.util.List;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import io.subutai.core.pluginmanager.api.dao.ConfigDataService;
import io.subutai.core.pluginmanager.api.model.PermissionJson;
import io.subutai.core.pluginmanager.api.model.PluginDetails;


public interface PluginManager
{
    void register( String name, String version, Attachment pathToKar, List<PermissionJson> permissions )
            throws IOException;

    ConfigDataService getConfigDataService();

    List<PluginDetails> getInstalledPlugins();

    void unregister( Long pluginId );

    void setPermissions( Long pluginId, String permissionJson );

    void update( String pluginId, String name, String version );
}
