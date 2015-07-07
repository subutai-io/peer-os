package io.subutai.common.quota;


/**
 * Unit types for ram quotas
 */
public enum RamQuotaUnit
{
    BYTE( "", "Bytes" ),
    KB( "K", "Kilobytes" ),
    MB( "M", "Megabytes" ),
    GB( "G", "Gigabytes" );


    private String acronym;
    private String name;


    RamQuotaUnit( final String acronym, final String name )
    {
        this.acronym = acronym;
        this.name = name;
    }


    public String getAcronym()
    {
        return acronym;
    }


    public String getName()
    {
        return name;
    }


    public static RamQuotaUnit parseFromAcronym( String acronym )
    {
        for ( RamQuotaUnit ramQuotaUnit : RamQuotaUnit.values() )
        {
            if ( ramQuotaUnit.getAcronym().equalsIgnoreCase( acronym ) )
            {
                return ramQuotaUnit;
            }
        }
        return null;
    }

}
