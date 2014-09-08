package org.safehaus.subutai.core.configuration.impl.utils;


import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.gson.JsonObject;
import org.safehaus.subutai.core.configuration.api.ConfigTypeEnum;
import org.safehaus.subutai.common.util.FileUtil;
import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author dilshat
 */
public class YamlParser implements ConfigParser {

	private final Map yaml;


	public YamlParser(String content) throws YamlException {

		YamlReader reader = new YamlReader(content);
		yaml = (Map) reader.read();
	}


	public String getStringProperty(String propertyName) {
		Object property = yaml.get(propertyName);
		if (property instanceof String) {
			return (String) yaml.get(propertyName);
		}
		return null;
	}


	public Map getMapProperty(String propertyName) {
		Object property = yaml.get(propertyName);
		if (property instanceof Map) {
			return (Map) yaml.get(propertyName);
		}
		return null;
	}


	public List getListProperty(String propertyName) {
		Object property = yaml.get(propertyName);
		if (property instanceof List) {
			return (List) yaml.get(propertyName);
		}
		return null;
	}


	public Object getProperty(String propertyName) {
		return yaml.get(propertyName);
	}


	public void setProperty(String propertyName, Object propertyValue) {
		yaml.put(propertyName, propertyValue);
	}


	public String getYaml() throws YamlException {
		StringWriter str = new StringWriter();
		YamlWriter writer = new YamlWriter(str);
		writer.write(yaml);
		writer.close();
		return str.toString();
	}


	@Override
	public JsonObject parserConfig(final String pathToConfig, final ConfigTypeEnum configTypeEnum) {
		ConfigBuilder configBuilder = new ConfigBuilder();
		JsonObject jo = configBuilder.getConfigJsonObject(pathToConfig, configTypeEnum);

		String content = FileUtil.getContent(pathToConfig, this);

		Yaml yaml = new Yaml();
		Map<String, Object> config = (Map<String, Object>) yaml.load(content);
		List<JsonObject> fields = new ArrayList<>();
		for (String key : config.keySet()) {
			Object value = config.get(key);
			JsonObject field = configBuilder.buildFieldJsonObject(key, "", true, "textfield", true, value.toString());
			fields.add(field);
		}

		JsonObject njo = configBuilder.addJsonArrayToConfig(jo, fields);
		return njo;
	}


    /*private String getValuePath( StringBuilder sb, Object obj ) {
        return sb.toString();
        if ( obj instanceof Map ) {
            Map m = ( Map ) obj;

            for ( Object o : m.keySet() ) {

                getValuePath( sb, obj );
            }
            getValuePath( sb, obj );
        }
    }*/
}