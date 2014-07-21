package org.safehaus.subutai.configuration.manager.impl.utils;


import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * @author dilshat
 */
public class IniParser {

    private final PropertiesConfiguration config;


    public IniParser( String content ) throws ConfigurationException {
        config = new PropertiesConfiguration();
        config.load( new ByteArrayInputStream( content.getBytes() ) );
    }


    public PropertiesConfiguration getConfig() {
        return config;
    }


    public Object getProperty( String propertyName ) {
        return config.getString( propertyName );
    }


    public String getStringProperty( String propertyName ) {
        return config.getString( propertyName );
    }


    public void setProperty( String propertyName, Object propertyValue ) {
        config.setProperty( propertyName, propertyValue );
    }


    public void addProperty( String propertyName, Object propertyValue ) {
        config.addProperty( propertyName, propertyValue );
    }


    public String getIni() throws ConfigurationException {
        StringWriter str = new StringWriter();
        config.save( str );
        return str.toString();
    }
}