package org.safehaus.subutai.configuration.manager.impl.utils;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.DefaultExpressionEngine;
import org.apache.commons.configuration.tree.ExpressionEngine;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;

/**
 *
 * @author dilshat
 */
public class XmlParser {

    private final XMLConfiguration config;
    private final ExpressionEngine defaultEngine = new DefaultExpressionEngine();
    private final ExpressionEngine expressEngine = new XPathExpressionEngine();

    public XmlParser(String content) throws ConfigurationException {
        config = new XMLConfiguration();
        config.load(new ByteArrayInputStream(content.getBytes()));
    }

    public Object getProperty(String propertyName) {
        return config.getProperty(propertyName);
    }

    public void setProperty(String propertyName, Object propertyValue) {
        config.setProperty(propertyName, propertyValue);
    }

    public String getStringProperty(String propertyName) {
        return config.getString(propertyName);
    }

    public String getConventionalProperty(String propertyName) {
        XMLConfiguration.setDefaultExpressionEngine(expressEngine);
        return config.getString(String.format("property[name='%s']/value", propertyName));
    }

    public void addConventionalProperty(String propertyName, Object propertyValue) {
        XMLConfiguration.setDefaultExpressionEngine(defaultEngine);
        config.addProperty("property(-1).name", propertyName);
        config.addProperty("property.value", propertyValue);
    }

    public void setConventionalProperty(String propertyName, Object propertyValue) {
        XMLConfiguration.setDefaultExpressionEngine(expressEngine);
        config.setProperty(String.format("property[name='%s']/value", propertyName), propertyValue);
    }

    public String getXml() throws ConfigurationException {
        StringWriter str = new StringWriter();
        config.save(str);
        return str.toString();
    }

}