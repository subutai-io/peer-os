package io.subutai.common.quota;


public enum MemoryUnit
{
    BYTES( "B", "Bytes", 0 ),
    KB( "K", "Kilobytes", 1 ),
    MB( "M", "Megabytes", 2 ),
    GB( "G", "Gigabytes", 3 ),
    TB( "T", "Terabytes", 4 ),
    PB( "P", "Petabytes", 5 ),
    EB( "E", "Exabytes", 6 ),
    ZB( "Z", "Zetabytes", 7 ),
    YB( "Y", "Yottabytes", 8 ),
    NONE( "none", "None", 9 );
    private String acronym;
    private String name;
    private int unitIdx;


    private MemoryUnit( String acronym, String name, int unitIdx )
    {
        this.acronym = acronym;
        this.name = name;
        this.unitIdx = unitIdx;
    }


    public String getAcronym()
    {
        return acronym;
    }


    public String getName()
    {
        return name;
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
                return KB;
            case 2:
                return MB;
            case 3:
                return GB;
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
            case "kb":
            case "KB":
            case "Kilobytes":
                return KB;
            case "mb":
            case "MB":
            case "Megabytes":
                return MB;
            case "gb":
            case "GB":
            case "Gigabytes":
                return GB;
            default:
                return null;
        }
    }
}
