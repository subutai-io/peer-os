package org.safehaus.subutai.configuration.manager.impl.utils;


import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.safehaus.subutai.configuration.manager.api.ConfigTypeEnum;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * @author dilshat
 */
public class IniParser implements ConfigParser {

	private final PropertiesConfiguration config;


	public IniParser(String content) throws ConfigurationException {
		config = new PropertiesConfiguration();
		config.load(new ByteArrayInputStream(content.getBytes()));
	}


	public PropertiesConfiguration getConfig() {
		return config;
	}


	//    @Override
	public Object getProperty(String propertyName) {
		return config.getString(propertyName);
	}


	public String getStringProperty(String propertyName) {
		return config.getString(propertyName);
	}


	public void setProperty(String propertyName, Object propertyValue) {
		config.setProperty(propertyName, propertyValue);
	}


	public void addProperty(String propertyName, Object propertyValue) {
		config.addProperty(propertyName, propertyValue);
	}


	public String getIni() throws ConfigurationException {
		StringWriter str = new StringWriter();
		config.save(str);
		return str.toString();
	}


	@Override
	public JsonObject parserConfig(String pathToConfig, ConfigTypeEnum configTypeEnum) {
		ConfigBuilder configBuilder = new ConfigBuilder();
		JsonObject jo = configBuilder.getConfigJsonObject(pathToConfig, configTypeEnum);

		Iterator<String> iterator = config.getKeys();
		List<JsonObject> fields = new ArrayList<>();
		while (iterator.hasNext()) {
			String key = iterator.next();
			String value = (String) config.getProperty(key);
			JsonObject field =
					configBuilder.buildFieldJsonObject(key.trim(), key.trim(), true, "textfield", true, value.trim());
			fields.add(field);
		}

		JsonObject njo = configBuilder.addJsonArrayToConfig(jo, fields);

		return njo;
	}


	public void setValuesFromJsonObject(JsonObject jsonObject) {
		JsonArray jsonArray = jsonObject.getAsJsonArray("configFields");
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject jo = (JsonObject) jsonArray.get(i);
			String fieldName = jo.getAsJsonPrimitive("fieldName").getAsString();
			String value = jo.getAsJsonPrimitive("value").getAsString();
		}
	}
}