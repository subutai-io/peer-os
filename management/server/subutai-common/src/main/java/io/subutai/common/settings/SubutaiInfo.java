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
            config = new PropertiesConfiguration( String.format( "%s/git.properties", Common
                        .KARAF_ETC ) );

        }
        catch ( ConfigurationException e )
        {
            LOG.error( "Error in loading git.properties file." );
            e.printStackTrace();
        }
        return config;
    }


    public static Object getCommitId()
    {
        return PROPERTIES.getProperty( "git.commit.id" );
    }


    public static Object getBranch()
    {
        return PROPERTIES.getProperty( "git.branch" );
    }


    public static Object getCommitterUserName()
    {
        return PROPERTIES.getProperty( "git.commit.user.name" );
    }


    public static Object getCommitterUserEmail()
    {
        return PROPERTIES.getProperty( "git.commit.user.email" );
    }


    public static Object getBuilderUserName()
    {
        return PROPERTIES.getProperty( "git.build.user.name" );
    }


    public static Object getBuilderUserEmail()
    {
        return PROPERTIES.getProperty( "git.build.user.email" );
    }


    public static Object getBuildTime()
    {
        return PROPERTIES.getProperty( "git.build.time" );
    }


    public static Object getVersion()
    {
        return PROPERTIES.getProperty( "git.build.version" );
    }
}


