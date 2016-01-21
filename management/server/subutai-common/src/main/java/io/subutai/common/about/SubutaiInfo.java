package io.subutai.common.about;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import io.subutai.common.settings.Common;


public class SubutaiInfo
{
    private static Properties PROPERTIES = loadProperties();

    public static Properties loadProperties()
    {
        Properties prop = new Properties();
        InputStream input = null;
        try
        {
            input = new FileInputStream( String.format( "%s/git.properties", Common.KARAF_ETC ) );
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


    public static String getCommitId()
    {
        return PROPERTIES.getProperty( "git.commit.id" );
    }


    public static String getBranch()
    {
        return PROPERTIES.getProperty( "git.branch" );
    }


    public static String getCommitterUserName()
    {
        return PROPERTIES.getProperty( "git.commit.user.name" );
    }


    public static String getCommitterUserEmail()
    {
        return PROPERTIES.getProperty( "git.commit.user.email" );
    }


    public static String getBuilderUserName()
    {
        return PROPERTIES.getProperty( "git.build.user.name" );
    }


    public static String getBuilderUserEmail()
    {
        return PROPERTIES.getProperty( "git.build.user.email" );
    }


    public static String getBuildTime()
    {
        return PROPERTIES.getProperty( "git.build.time" );
    }


    public static String getVersion()
    {
        return PROPERTIES.getProperty( "git.build.version" );
    }
}


