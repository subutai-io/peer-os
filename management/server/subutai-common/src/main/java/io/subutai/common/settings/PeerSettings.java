package io.subutai.common.settings;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by ermek on 2/6/16.
 */
public class PeerSettings
{
    private static Properties PROPERTIES = loadProperties();

    public static Properties loadProperties()
    {
        Properties prop = new Properties();
        InputStream input = null;
        try
        {
            input = new FileInputStream( String.format( "%s/peer.cfg", Common.KARAF_ETC ) );
            prop.load( input );
        }
        catch ( IOException ex )
        {
            ex.printStackTrace();
        }
        finally
        {
            if ( input != null )
            {
                try
                {
                    input.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }

    public static String getExternalIpInterface()
    {
        return PROPERTIES.getProperty( "externalIpInterface" );
    }


    public static String getEncryptionState()
    {
        return PROPERTIES.getProperty( "encryptionEnabled" );
    }


    public static String getRestEncryptionState()
    {
        return PROPERTIES.getProperty( "restEncryptionEnabled" );
    }


    public static String getIntegrationState()
    {
        return PROPERTIES.getProperty( "integrationEnabled" );
    }


    public static String getKeyTrustCheckState()
    {
        return PROPERTIES.getProperty( "keyTrustCheckEnabled" );
    }


}
