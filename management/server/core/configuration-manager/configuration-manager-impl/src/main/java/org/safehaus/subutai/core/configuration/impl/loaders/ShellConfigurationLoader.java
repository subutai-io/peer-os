package org.safehaus.subutai.core.configuration.impl.loaders;


import org.safehaus.subutai.core.configuration.api.TextInjector;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 8/4/14.
 */
public class ShellConfigurationLoader implements ConfigurationLoader
{


    private TextInjector textInjector;


    public ShellConfigurationLoader( final TextInjector textInjector )
    {
        this.textInjector = textInjector;
    }


    @Override
    public JsonObject getConfiguration( String hostname, String configPathFilename )
    {
        return null;
    }


    @Override
    public boolean setConfiguration( String hostname, String configFilePath, String jsonObjectConfig )
    {
        return false;
    }
}
