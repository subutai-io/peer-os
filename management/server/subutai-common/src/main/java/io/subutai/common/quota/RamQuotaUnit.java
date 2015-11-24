package io.subutai.common.quota;


/**
 * Unit types for ram quotas
 */
public enum RamQuotaUnit
{
    BYTE( "", "Bytes", 1 ),
    KB( "K", "Kilobytes", 1024 ),
    MB( "M", "Megabytes", 1024 * 1024 ),
    GB( "G", "Gigabytes", 1024 * 1024 * 1024 );


    private String acronym;
    private String name;
    private int multiplicator;


    RamQuotaUnit( final String acronym, final String name, final int multiplicator )
    {
        this.acronym = acronym;
        this.name = name;
        this.multiplicator = multiplicator;
    }


    public String getAcronym()
    {
        return acronym;
    }


    public String getName()
    {
        return name;
    }


    public int getMultiplicator()
    {
        return multiplicator;
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
