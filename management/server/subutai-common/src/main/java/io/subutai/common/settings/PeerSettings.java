package io.subutai.common.settings;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Created by ermek on 2/6/16.
 */
public class PeerSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerSettings.class );
    private static PropertiesConfiguration PROPERTIES = loadProperties();

    public static PropertiesConfiguration loadProperties()
    {
        PropertiesConfiguration config = null;
        try
        {
            config = new PropertiesConfiguration( String.format( "%s/peer.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading peer.cfg file." );
            e.printStackTrace();
        }
        return config;
    }


    public static Object getExternalIpInterface()
    {
        return PROPERTIES.getProperty( "externalIpInterface" );
    }


    public static Object getEncryptionState()
    {
        return PROPERTIES.getProperty( "encryptionEnabled" );
    }


    public static Object getRestEncryptionState()
    {
        return PROPERTIES.getProperty( "restEncryptionEnabled" );
    }


    public static Object getIntegrationState()
    {
        return PROPERTIES.getProperty( "integrationEnabled" );
    }


    public static Object getKeyTrustCheckState()
    {
        return PROPERTIES.getProperty( "keyTrustCheckEnabled" );
    }
}
