package org.safehaus.subutai.configuration.manager.impl.loaders;


import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 8/4/14.
 */
public class PlainConfigurationLoader implements ConfigurationLoader {
    @Override
    public JsonObject getConfiguration( String hostname, String configPathFilename ) {
        return null;
    }


    @Override
    public boolean setConfiguration( String hostname, String configFilePath, String jsonObjectConfig ) {
        return false;
    }
}
