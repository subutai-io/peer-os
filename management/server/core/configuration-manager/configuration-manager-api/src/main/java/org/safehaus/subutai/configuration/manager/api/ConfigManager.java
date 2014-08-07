/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.api;


import com.google.gson.JsonObject;


/**
 *
 */
public interface ConfigManager {

    public boolean injectConfiguration( String hostname, String configFilePath, String config,
                                        ConfigTypeEnum configTypeEnum );

    public JsonObject getConfiguration( String hostname, String configPathFilename,
                                        ConfigTypeEnum configTypeEnum );

    public String getProperty( JsonObject config, String key, ConfigTypeEnum configTypeEnum );

    public void setProperty( JsonObject config, String key, String value, ConfigTypeEnum configTypeEnum );


    public JsonObject getJsonObjectFromResources( String configPathFilename, ConfigTypeEnum configTypeEnum );
}
