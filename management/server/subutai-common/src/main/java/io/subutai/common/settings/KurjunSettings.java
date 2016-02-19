package io.subutai.common.settings;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Created by ermek on 2/6/16.
 */
public class KurjunSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( KurjunSettings.class );
    private static PropertiesConfiguration PROPERTIES = loadProperties();


    public static PropertiesConfiguration loadProperties()
    {
        PropertiesConfiguration config = null;
        try
        {
            config = new PropertiesConfiguration( String.format( "%s/kurjun.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading kurjun.cfg file." );
            e.printStackTrace();
        }
        return config;
    }


    public static List<String> getGlobalKurjunUrls()
    {
        String urls = String.valueOf( PROPERTIES.getProperty( "globalKurjunUrls" ) );
        String replace = urls.replace( "[", "" );
        String replace1 = replace.replace( "]", "" );

        return new ArrayList<String>( Arrays.asList( replace1.split( "," ) ) );
    }


    public static void setSettings( String urls )
    {
        try
        {
            PROPERTIES.setProperty( "globalKurjunUrls", urls );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving kurjun.cfg file." );
            e.printStackTrace();
        }
    }
}
