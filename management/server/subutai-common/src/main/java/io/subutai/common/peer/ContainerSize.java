package io.subutai.common.peer;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Container size enumeration
 */
public enum ContainerSize
{
    TINY, SMALL, MEDIUM, LARGE, HUGE;


    @JsonIgnore
    public static Set getContainerSizeDescription() throws IOException
    {
        Properties prop = new Properties();
        InputStream is = new FileInputStream( System.getProperty( "karaf.etc" ) + "/quota.cfg" );
        prop.load( is );
        return prop.entrySet();
    }
}
