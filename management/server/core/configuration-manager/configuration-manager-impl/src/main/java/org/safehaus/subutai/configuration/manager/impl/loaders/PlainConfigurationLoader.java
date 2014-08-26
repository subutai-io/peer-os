package org.safehaus.subutai.configuration.manager.impl.loaders;


import com.google.gson.JsonObject;
import org.safehaus.subutai.configuration.manager.api.TextInjector;


/**
 * Created by bahadyr on 8/4/14.
 */
public class PlainConfigurationLoader implements ConfigurationLoader {

	private TextInjector textInjector;


	public PlainConfigurationLoader(final TextInjector textInjector) {
		this.textInjector = textInjector;
	}


	@Override
	public JsonObject getConfiguration(String hostname, String configPathFilename) {
		return null;
	}


	@Override
	public boolean setConfiguration(String hostname, String configFilePath, String content) {
		textInjector.echoTextIntoAgent(hostname, configFilePath, content);
		return true;
	}
}
