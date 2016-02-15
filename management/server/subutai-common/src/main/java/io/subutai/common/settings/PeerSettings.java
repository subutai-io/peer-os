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


    public static String getExternalIpInterface()
    {
        return String.valueOf( PROPERTIES.getProperty( "externalIpInterface" ) );
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


    public static boolean isRegisteredToHub()
    {
        String state = String.valueOf( PROPERTIES.getProperty( "isRegisteredToHub" ) );
        return Objects.equals( state, "true" );
    }


    public static void setRegisterToHubState( boolean registrationState )
    {
        try
        {
            PROPERTIES.setProperty( "isRegisteredToHub", registrationState );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setExternalIpInterface( String externalIpInterface )
    {
        try
        {
            PROPERTIES.setProperty( "externalIpInterface", externalIpInterface );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setEncryptionState( boolean encryptionEnabled )
    {
        try
        {
            PROPERTIES.setProperty( "encryptionEnabled", encryptionEnabled );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setRestEncryptionState( boolean restEncryptionEnabled )
    {
        try
        {
            PROPERTIES.setProperty( "restEncryptionEnabled", restEncryptionEnabled );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setIntegrationState( boolean integrationEnabled )
    {
        try
        {
            PROPERTIES.setProperty( "integrationEnabled", integrationEnabled );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setKeyTrustCheckState( boolean keyTrustCheckEnabled )
    {
        try
        {
            PROPERTIES.setProperty( "keyTrustCheckEnabled", keyTrustCheckEnabled );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }
}
