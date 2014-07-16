package org.safehaus.subutai.configuration.manager.impl.utils;


import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;


/**
 * Created by bahadyr on 7/16/14.
 */
public class XMLConfigurationLoader implements ConfigurationLoader {


    public void loadConfig() {
        try {
            String xmlConfig = "/home/bahadyr/Desktop/products/hadoop-1.2.1/conf/core-site.xml";
            XMLConfiguration config = new XMLConfiguration( xmlConfig );
            Document document = config.getDocument();
            NodeList nodeList = document.getChildNodes();
            Node node = document.getFirstChild();
            System.out.println(node.getTextContent());
        }
        catch ( ConfigurationException e ) {
            e.printStackTrace();
        }
    }


    @Override
    public Object getConfiguration() {
        return null;
    }
}
