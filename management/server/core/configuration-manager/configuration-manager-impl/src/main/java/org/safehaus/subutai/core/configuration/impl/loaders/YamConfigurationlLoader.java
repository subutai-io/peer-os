package org.safehaus.subutai.core.configuration.impl.loaders;


import org.safehaus.subutai.core.configuration.api.TextInjector;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/9/14.
 */
public class YamConfigurationlLoader implements ConfigurationLoader
{


    private TextInjector textInjector;


    public YamConfigurationlLoader( final TextInjector textInjector )
    {
        this.textInjector = textInjector;
    }


    @Override
    public JsonObject getConfiguration( String hostname, String configPathFilename )
    {
        //TODO cat file from given agent, convert to required format, detect types and form a Config
        JsonObject jsonObject = new JsonObject();
        // TODO iterate through yaml to set Config field values
        return jsonObject;
    }


    @Override
    public boolean setConfiguration( String hostname, String configFilePath, String config )
    {
        // TODO Read config from instance
        // TODO set values to yaml object from Config
        String newContent = ""; // yaml to string

        // TODO inject Config
        textInjector.echoTextIntoAgent( hostname, "path", newContent );
        return true;
    }
}
