package io.subutai.core.bazaar.api;


import java.util.List;

import io.subutai.core.bazaar.api.model.Plugin;
import io.subutai.core.hubmanager.api.exception.HubManagerException;


public interface Bazaar
{
    String getChecksum();

    String getProducts();

    List<Plugin> getPlugins();

    void installPlugin( String name, String version, String kar, String url, String uid ) throws HubManagerException;

    void uninstallPlugin( Long id, String name );

    void restorePlugin( Long id, String name, String version, String kar, String url, String uid )
            throws HubManagerException;
}
