package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;

/**
 *
 * @author dilshat
 */
public class YamlParser {

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

}