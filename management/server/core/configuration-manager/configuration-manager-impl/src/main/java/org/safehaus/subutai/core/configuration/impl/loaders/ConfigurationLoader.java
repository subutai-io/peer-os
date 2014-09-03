package org.safehaus.subutai.core.configuration.impl.loaders;


import com.google.gson.JsonObject;


/**
 * Created by bahadyr on 7/16/14.
 */
public interface ConfigurationLoader {

	public JsonObject getConfiguration(String hostname, String configPathFilename);

	public boolean setConfiguration(String hostname, String configFilePath, String jsonObjectConfig);
}
