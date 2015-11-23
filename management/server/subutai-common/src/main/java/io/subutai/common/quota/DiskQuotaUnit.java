package io.subutai.common.quota;


import java.math.BigDecimal;
import java.math.BigInteger;


/**
 * Unit types for disk quotas
 */
public enum DiskQuotaUnit
{
    BYTE( "", "Bytes", new BigDecimal( "1.00" ) ),
    KB( "K", "Kilobytes", new BigDecimal( "1024.00" ) ),
    MB( "M", "Megabytes", new BigDecimal( "1048576.00" ) ),
    GB( "G", "Gigabytes", new BigDecimal( "1073741824.00" ) ),
    TB( "T", "Terabytes", new BigDecimal( "1099511627776.00" ) ),
    PB( "P", "Petabytes", new BigDecimal( "1125899906842624.00" ) ),
    EB( "E", "Exabytes", new BigDecimal( "1152921504606846976.00" ) ),
    UNLIMITED( "none", "Unlimited", null );
    private String acronym;
    private String name;
    private BigDecimal multiplicator;


    private DiskQuotaUnit( final String acronym, final String name, final BigDecimal multiplicator )
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


    public BigDecimal getMultiplicator()
    {
        return multiplicator;
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
