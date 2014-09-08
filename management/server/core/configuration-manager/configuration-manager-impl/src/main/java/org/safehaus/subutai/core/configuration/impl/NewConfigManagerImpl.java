package org.safehaus.subutai.core.configuration.impl;


import org.safehaus.subutai.core.configuration.api.ConfigManager;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;

import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 9/8/14.
 */
public class NewConfigManagerImpl implements ConfigManager{
    @Override
    public boolean injectConfiguration( final String hostname, final String configFilePath, final String config,
                                        final ConfigTypeEnum configTypeEnum ) {
        return false;
    }


    @Override
    public JsonObject getConfiguration( final String hostname, final String configPathFilename,
                                        final ConfigTypeEnum configTypeEnum ) {
        return null;
    }


    @Override
    public String getProperty( final JsonObject config, final String key, final ConfigTypeEnum configTypeEnum ) {
        return null;
    }


    @Override
    public void setProperty( final JsonObject config, final String key, final String value,
                             final ConfigTypeEnum configTypeEnum ) {

    }


    @Override
    public JsonObject getJsonObjectFromResources( final String configPathFilename,
                                                  final ConfigTypeEnum configTypeEnum ) {
        return null;
    }
}
