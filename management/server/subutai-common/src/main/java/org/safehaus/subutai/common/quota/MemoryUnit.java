package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 10/8/14.
 */
public enum MemoryUnit
{
    BYTES( "B", "Bytes", 0 ),
    KILOBYTES( "K", "Kilobytes", 1 ),
    MEGABYTES( "M", "Megabytes", 2 ),
    GIGABYTES( "G", "Gigabytes", 3 ),
    TERABYTES( "T", "Terabytes", 4 ),
    PETABYTES( "P", "Petabytes", 5 ),
    EXABYTES( "E", "Exabytes", 6 ),
    ZETTABYTES( "Z", "Zetabytes", 7 ),
    YOTTABYTES( "Y", "Yottabytes", 8 ),
    NONE( "none", "None", 9 );
    private String shortName;
    private String longName;
    private int unitIdx;


    private MemoryUnit( String shortName, String longName, int unitIdx )
    {
        this.shortName = shortName;
        this.longName = longName;
        this.unitIdx = unitIdx;
    }


    public String getShortName()
    {
        return shortName;
    }


    public String getLongName()
    {
        return longName;
    }


    public int getUnitIdx()
    {
        return unitIdx;
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
            case "B":
            case "Bytes":
                return BYTES;
            case "k":
            case "K":
            case "Kilobytes":
                return KILOBYTES;
            case "m":
            case "M":
            case "Megabytes":
                return MEGABYTES;
            case "g":
            case "G":
            case "Gigabytes":
                return GIGABYTES;
            default:
                return null;
        }
    }
}
