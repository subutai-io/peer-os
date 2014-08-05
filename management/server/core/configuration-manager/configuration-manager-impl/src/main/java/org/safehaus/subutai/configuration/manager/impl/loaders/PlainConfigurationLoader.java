package org.safehaus.subutai.configuration.manager.impl.loaders;


import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 8/4/14.
 */
public class PlainConfigurationLoader implements ConfigurationLoader {
    @Override
    public JsonObject getConfiguration( final Agent agent, final String configPathFilename ) {
        return null;
    }


    @Override
    public boolean setConfiguration( final Agent agent, final String configFilePath, final String jsonObjectConfig ) {
        return false;
    }
}
