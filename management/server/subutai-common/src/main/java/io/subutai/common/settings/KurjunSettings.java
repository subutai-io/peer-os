package io.subutai.common.settings;


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
            LOG.error( "Error in loading peer.cfg file." );
            e.printStackTrace();
        }
        return config;
    }

    public static Object getGlobalKurjunUrls()
    {
        return PROPERTIES.getProperty( "globalKurjunUrls" );
    }


}
