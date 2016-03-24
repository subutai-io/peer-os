package io.subutai.core.kurjun.manager.impl.utils;


import java.io.IOException;
import java.util.Properties;


/**
 *
 */
public class PropertyUtils
{
    private static Properties properties = null;


    public void PropertyUtils()
    {
        properties = loadProperties();
    }

    public Properties loadProperties()
    {
        Properties _prop = new Properties();
        try
        {
            _prop.load( this.getClass().getClassLoader().getResourceAsStream( "config.properties"));

            return _prop;

        }
        catch (IOException ex)
        {
            return null;
        }
    }

    public static String getValue(String value)
    {
        return properties.getProperty(value);
    }
}
