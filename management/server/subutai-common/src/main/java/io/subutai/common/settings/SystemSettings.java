package io.subutai.common.settings;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


/**
 * Created by ermek on 2/19/16.
 */
public class SystemSettings
{
    private static final Logger LOG = LoggerFactory.getLogger( SystemSettings.class );
    private static PropertiesConfiguration PROPERTIES = loadProperties();


    public static PropertiesConfiguration loadProperties()
    {
        PropertiesConfiguration config = null;
        try
        {
            config = new PropertiesConfiguration( String.format( "%s/subutaisystem.cfg", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading subutaisettings.cfg file." );
            e.printStackTrace();
        }
        return config;
    }

    // Kurjun Settings


    public static List<String> getGlobalKurjunUrls()
    {
        String urls = String.valueOf( PROPERTIES.getProperty( "globalKurjunUrls" ) );
        String replace = urls.replace( "[", "" );
        String replace1 = replace.replace( "]", "" );

        return new ArrayList<String>( Arrays.asList( replace1.split( "," ) ) );
    }


    public static void setGlobalKurjunUrls( String urls )
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

    // Network Settings


    public static String getExternalIpInterface()
    {
        return String.valueOf( PROPERTIES.getProperty( "externalInterfaceName" ) );
    }


    public static void setExternalIpInterface( String externalInterfaceName )
    {
        try
        {
            PROPERTIES.setProperty( "externalInterfaceName", externalInterfaceName );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static int getOpenPort()
    {
        return Integer.valueOf( String.valueOf( PROPERTIES.getProperty( "openPort" ) ) );
    }


    public static int getSecurePortX1()
    {
        return Integer.valueOf( String.valueOf( PROPERTIES.getProperty( "securePortX1" ) ) );
    }


    public static int getSecurePortX2()
    {
        return Integer.valueOf( String.valueOf( PROPERTIES.getProperty( "securePortX2" ) ) );
    }


    public static int getSecurePortX3()
    {
        return Integer.valueOf( String.valueOf( PROPERTIES.getProperty( "securePortX3" ) ) );
    }


    public static int getSpecialPortX1()
    {
        return Integer.valueOf( String.valueOf( PROPERTIES.getProperty( "specialPortX1" ) ) );
    }


    public static void setOpenPort( int openPort )
    {
        try
        {
            PROPERTIES.setProperty( "openPort", openPort );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setSecurePortX1( int securePortX1 )
    {
        try
        {
            PROPERTIES.setProperty( "securePortX1", securePortX1 );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setSecurePortX2( int securePortX2 )
    {
        try
        {
            PROPERTIES.setProperty( "securePortX2", securePortX2 );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setSecurePortX3( int securePortX3 )
    {
        try
        {
            PROPERTIES.setProperty( "securePortX3", securePortX3 );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    public static void setSpecialPortX1( int specialPortX1 )
    {
        try
        {
            PROPERTIES.setProperty( "specialPortX1", specialPortX1 );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }


    // Security Settings


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


    // Peer Settings


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


    public static String getPublicUrl()
    {
        return String.valueOf( PROPERTIES.getProperty( "publicURL" ) );
    }


    public static void setPublicUrl( String publicUrl )
    {
        try
        {
            PROPERTIES.setProperty( "publicUrl", publicUrl );
            PROPERTIES.save();
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in saving peer.cfg file." );
            e.printStackTrace();
        }
    }
}

