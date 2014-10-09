package org.safehaus.subutai.core.lxc.quota.api;


/**
 * Created by talas on 10/8/14.
 */
public enum MemoryUnit
{
    BYTES( "b", "Bytes", 0 ),
    KILOBYTES( "k", "Kilobytes", 1 ),
    MEGABYTES( "m", "Megabytes", 2 ),
    GIGABYTES( "g", "Gigabytes", 3 );
    private String shortName;
    private String longName;
    private int value;


    private MemoryUnit( String shortName, String longName, int value )
    {
        this.shortName = shortName;
        this.longName = longName;
        this.value = value;
    }


    public String getShortName()
    {
        return shortName;
    }


    public String getLongName()
    {
        return longName;
    }


    public int getValue()
    {
        return value;
    }


    public static MemoryUnit getMemoryUnit( int value )
    {
        switch ( value )
        {
            case 0:
                return BYTES;
            case 1:
                return KILOBYTES;
            case 2:
                return MEGABYTES;
            case 3:
                return GIGABYTES;
            default:
                return null;
        }
    }


    public static MemoryUnit getMemoryUnit( String unit )
    {
        switch ( unit )
        {
            case "b":
            case "Bytes":
                return BYTES;
            case "k":
            case "Kilobytes":
                return KILOBYTES;
            case "m":
            case "Megabytes":
                return MEGABYTES;
            case "g":
            case "Gigabytes":
                return GIGABYTES;
            default:
                return null;
        }
    }
}
