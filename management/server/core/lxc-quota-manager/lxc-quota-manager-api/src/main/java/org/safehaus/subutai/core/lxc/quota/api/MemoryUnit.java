package org.safehaus.subutai.core.lxc.quota.api;


/**
 * Created by talas on 10/8/14.
 */
public enum MemoryUnit
{
    BYTES( "b", "Bytes" ),
    KILOBYTES( "k", "Kilobytes" ),
    MEGABYTES( "m", "Megabytes" ),
    GIGABYTES( "g", "Gigabytes" );
    private String shortName;
    private String longName;


    private MemoryUnit( String shortName, String longName )
    {
        this.shortName = shortName;
        this.longName = longName;
    }


    public String getShortName()
    {
        return shortName;
    }


    public String getLongName()
    {
        return longName;
    }
}
