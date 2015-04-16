package org.safehaus.subutai.common.quota;


/**
 * Unit types for disk quotas
 */
public enum DiskQuotaUnit
{
    BYTE( "", "Bytes" ),
    KB( "K", "Kilobytes" ),
    MB( "M", "Megabytes" ),
    GB( "G", "Gigabytes" ),
    TB( "T", "Terabytes" ),
    PB( "P", "Petabytes" ),
    EB( "E", "Exabytes" ),
    UNLIMITED( "none", "Unlimited" );
    private String acronym;
    private String name;


    private DiskQuotaUnit( final String acronym, final String name )
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


    public static DiskQuotaUnit parseFromAcronym( String acronym )
    {
        for ( DiskQuotaUnit diskQuotaUnit : DiskQuotaUnit.values() )
        {
            if ( diskQuotaUnit.getAcronym().equalsIgnoreCase( acronym ) )
            {
                return diskQuotaUnit;
            }
        }
        return null;
    }
}
