package io.subutai.core.bazaar.api;


import java.io.IOException;
import java.util.List;

import io.subutai.core.bazaar.api.model.Plugin;


public interface Bazaar
{
    String getChecksum();

    String getProducts();

    List<Plugin> getPlugins();

    void installPlugin( String name, String version, String kar, String url, String uid ) throws IOException;

    void uninstallPlugin( Long id, String name );

    void restorePlugin( Long id, String name, String version, String kar, String url, String uid ) throws IOException;
}
