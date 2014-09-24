/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.configuration.api;


import com.google.gson.JsonObject;


/**
 *
 */
public interface ConfigurationManager
{

    public boolean injectConfiguration( String hostname, String configFilePath, String config,
                                        ConfiguraitonTypeEnum configuraitonTypeEnum );

    public JsonObject getConfiguration( String hostname, String configPathFilename, ConfiguraitonTypeEnum configuraitonTypeEnum );

    public String getProperty( JsonObject config, String key, ConfiguraitonTypeEnum configuraitonTypeEnum );

    public void setProperty( JsonObject config, String key, String value, ConfiguraitonTypeEnum configuraitonTypeEnum );


    public JsonObject getJsonObjectFromResources( String configPathFilename, ConfiguraitonTypeEnum configuraitonTypeEnum );
}
