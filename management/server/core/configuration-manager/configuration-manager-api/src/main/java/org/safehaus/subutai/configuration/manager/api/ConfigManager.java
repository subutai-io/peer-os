/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.configuration.manager.api;


import org.safehaus.subutai.shared.protocol.Agent;

import com.google.gson.JsonObject;


/**
 *
 */
public interface ConfigManager {

    public boolean injectConfiguration( Agent agent, String configFilePath, String config, ConfigTypeEnum configTypeEnum );

    public String getProperty( JsonObject config, String key, ConfigTypeEnum configTypeEnum );

    public void setProperty( JsonObject config, String key, String value, ConfigTypeEnum configTypeEnum  );

    public JsonObject getConfiguration( Agent agent, String configPathFilename, ConfigTypeEnum configTypeEnum );

    public JsonObject getJsonObjectFromResources( String configPathFilename, ConfigTypeEnum configTypeEnum );
}
