package io.subutai.bazaar.share.resource;


import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Byte measure unit types
 */
public enum ByteUnit
{
    BYTE( "", "Bytes", new BigDecimal( "1.000" ) ),
    KB( "KiB", "Kilobytes", new BigDecimal( "1024.000" ) ),
    MB( "MiB", "Megabytes", new BigDecimal( "1048576.000" ) ),
    GB( "GiB", "Gigabytes", new BigDecimal( "1073741824.000" ) ),
    TB( "TiB", "Terabytes", new BigDecimal( "1099511627776.000" ) ),
    PB( "PiB", "Petabytes", new BigDecimal( "1125899906842624.000" ) ),
    EB( "EiB", "Exabytes", new BigDecimal( "1152921504606846976.000" ) ),
    UNLIMITED( "none", "Unlimited", null );
    @JsonProperty( "acronym" )
    private String acronym;
    private String name;
    private BigDecimal multiplier;


    ByteUnit( final String acronym, final String name, final BigDecimal multiplier )
    {
        this.acronym = acronym;
        this.name = name;
        this.multiplier = multiplier;
    }


    public String getAcronym()
    {
        return acronym;
    }


    public String getName()
    {
        return name;
    }


    public BigDecimal getMultiplier()
    {
        return multiplier;
    }


    public static ByteUnit parseFromAcronym( String acronym )
    {
        for ( ByteUnit byteUnit : ByteUnit.values() )
        {
            if ( byteUnit.getAcronym().equalsIgnoreCase( acronym ) )
            {
                return byteUnit;
            }
        }
        return null;
    }

}
