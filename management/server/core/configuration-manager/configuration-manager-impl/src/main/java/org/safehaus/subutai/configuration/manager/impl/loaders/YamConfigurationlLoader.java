package org.safehaus.subutai.configuration.manager.impl.loaders;


import com.google.gson.JsonObject;
import org.safehaus.subutai.configuration.manager.api.TextInjector;
import org.yaml.snakeyaml.Yaml;


/**
 * Created by bahadyr on 7/9/14.
 */
public class YamConfigurationlLoader implements ConfigurationLoader {


//    private Agent agent;
//
//    public YamConfigurationlLoader(Agent agent) {
//        this.agent = agent;
//    }

	private TextInjector textInjector;


	public YamConfigurationlLoader(final TextInjector textInjector) {
		this.textInjector = textInjector;
	}


	@Override
	public JsonObject getConfiguration(String hostname, String configPathFilename) {

		//TODO cat file from given agent, convert to required format, detect types and form a Config
		Yaml yaml = new Yaml();
		Object result = yaml.loadAs(configPathFilename, Object.class);

		//
		JsonObject jsonObject = new JsonObject();

		// TODO iterate through yaml to set Config field values
		return jsonObject;
	}


	@Override
	public boolean setConfiguration(String hostname, String configFilePath, String config) {
		// TODO Read config from instance
//        Agent agent = null;
		String content = textInjector.catFile(hostname, "");

		// TODO set values to yaml object from Config
		Yaml yaml = new Yaml();
		Object result = yaml.loadAs(content, Object.class);

		String newContent = ""; // yaml to string

		// TODO inject Config
		textInjector.echoTextIntoAgent(hostname, "path", newContent);
		return true;
	}
}
