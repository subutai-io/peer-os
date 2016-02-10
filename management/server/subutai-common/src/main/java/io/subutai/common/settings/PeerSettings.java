package io.subutai.common.settings;


import java.util.Objects;

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


    public static boolean getEncryptionState()
    {
        String state = String.valueOf( PROPERTIES.getProperty( "encryptionEnabled" ) );
        return Objects.equals( state, "true" );
    }


    public static boolean getRestEncryptionState()
    {
        String state = String.valueOf( PROPERTIES.getProperty( "restEncryptionEnabled" ) );
        return Objects.equals( state, "true" );
    }


    public static boolean getIntegrationState()
    {
        String state = String.valueOf( PROPERTIES.getProperty( "integrationEnabled" ) );
        return Objects.equals( state, "true" );
    }


    public static boolean getKeyTrustCheckState()
    {
        String state = String.valueOf( PROPERTIES.getProperty( "keyTrustCheckEnabled" ) );
        return Objects.equals( state, "true" );
    }


    public static void setSettings( final String externalIpInterface, final boolean encryptionState,
                                    final boolean restEncryptionState, final boolean integrationState,
                                    final boolean keyTrustCheckState )
    {
        try
        {
            PROPERTIES.setProperty( "externalIpInterface", externalIpInterface );
            PROPERTIES.setProperty( "encryptionEnabled", encryptionState );
            PROPERTIES.setProperty( "restEncryptionEnabled", restEncryptionState );
            PROPERTIES.setProperty( "integrationEnabled", integrationState );
            PROPERTIES.setProperty( "keyTrustCheckEnabled", keyTrustCheckState );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            e.printStackTrace();
        }
    }
}
