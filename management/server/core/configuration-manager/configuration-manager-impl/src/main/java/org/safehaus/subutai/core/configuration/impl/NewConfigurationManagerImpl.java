package org.safehaus.subutai.core.configuration.impl;


import org.safehaus.subutai.core.configuration.api.ConfigurationManager;
import org.safehaus.subutai.core.configuration.api.ConfiguraitonTypeEnum;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 9/8/14.
 */
public class NewConfigurationManagerImpl implements ConfigurationManager
{
    @Override
    public boolean injectConfiguration( final String hostname, final String configFilePath, final String config,
                                        final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        return false;
    }


    @Override
    public JsonObject getConfiguration( final String hostname, final String configPathFilename,
                                        final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        return null;
    }


    @Override
    public String getProperty( final JsonObject config, final String key, final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        return null;
    }


    @Override
    public void setProperty( final JsonObject config, final String key, final String value,
                             final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        // TODO describe method
    }


    @Override
    public JsonObject getJsonObjectFromResources( final String configPathFilename, final ConfiguraitonTypeEnum configuraitonTypeEnum )
    {
        return null;
    }
}
