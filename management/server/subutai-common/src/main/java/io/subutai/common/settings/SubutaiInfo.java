package io.subutai.common.settings;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class SubutaiInfo
{
    private static final Logger LOG = LoggerFactory.getLogger( SubutaiInfo.class );
    private static PropertiesConfiguration PROPERTIES = loadProperties();


    public static PropertiesConfiguration loadProperties()
    {
        PropertiesConfiguration config = null;
        try
        {
            config = new PropertiesConfiguration( String.format( "%s/git.properties", Common.KARAF_ETC ) );
        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading git.properties file." );
            e.printStackTrace();
        }
        return config;
    }


    public static String getCommitId()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.commit.id" ) );
    }


    public static String getBranch()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.branch" ) );
    }


    public static String getCommitterUserName()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.commit.user.name" ));
    }


    public static String getCommitterUserEmail()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.commit.user.email" ));
    }


    public static String getBuilderUserName()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.build.user.name" ));
    }


    public static String getBuilderUserEmail()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.build.user.email" ));
    }


    public static String getBuildTime()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.build.time" ));
    }


    public static String getVersion()
    {
        return String.valueOf( PROPERTIES.getProperty( "git.build.version" ));
    }
}


