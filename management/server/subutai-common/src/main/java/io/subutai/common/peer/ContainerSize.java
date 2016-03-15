package io.subutai.common.peer;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Set;
import java.util.Properties;

/**
 * Container size enumeration
 */
public enum ContainerSize
{
    TINY, SMALL, MEDIUM, LARGE, HUGE;

    public static Set getConteinerSizeDescription() throws Exception
    {
        Properties prop = new Properties();
        InputStream is = new FileInputStream(System.getProperty( "karaf.etc" ) + "/quota.cfg" );
        prop.load(is);
        return prop.entrySet();
    }
}
