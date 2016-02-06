package io.subutai.common.settings;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * Created by ermek on 2/6/16.
 */
public class KurjunSettings
{
    private static Properties PROPERTIES = loadProperties();

    public static Properties loadProperties()
    {
        Properties prop = new Properties();
        InputStream input = null;
        try
        {
            input = new FileInputStream( String.format( "%s/kurjun.cfg", Common.KARAF_ETC ) );
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

    public static String getGlobalKurjunUrls()
    {
        return PROPERTIES.getProperty( "globalKurjunUrls" );
    }


}
