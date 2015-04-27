package org.safehaus.subutai.common.quota;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * RAM quota
 */
public class RamQuota extends MemoryQuotaInfo
{
    private static final String QUOTA_REGEX = "(\\d+)(K|M|G)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private RamQuotaUnit ramQuotaUnit;
    private int ramQuotaValue;


    public RamQuota( final RamQuotaUnit ramQuotaUnit, final int ramQuotaValue )
    {
        this.ramQuotaUnit = ramQuotaUnit;
        this.ramQuotaValue = ramQuotaValue;
    }


    public RamQuotaUnit getRamQuotaUnit()
    {
        return ramQuotaUnit;
    }


    public int getRamQuotaValue()
    {
        return ramQuotaValue;
    }


    public static RamQuota parse( String quotaString )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( quotaString ), "Invalid quota string" );


        Matcher quotaMatcher = QUOTA_PATTERN.matcher( quotaString.trim() );
        if ( quotaMatcher.matches() )
        {
            String quotaValue = quotaMatcher.group( 1 );
            int value = Integer.parseInt( quotaValue );
            String acronym = quotaMatcher.group( 2 );
            RamQuotaUnit ramQuotaUnit = RamQuotaUnit.parseFromAcronym( acronym );
            return new RamQuota( ramQuotaUnit == null ? RamQuotaUnit.BYTE : ramQuotaUnit, value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Unparseable result: %s", quotaString ) );
        }
    }


    @Override
    public String getQuotaKey()
    {
        return QuotaType.QUOTA_TYPE_RAM.getKey();
    }


    @Override
    public QuotaType getQuotaType()
    {
        return QuotaType.QUOTA_TYPE_RAM;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof RamQuota ) )
        {
            return false;
        }

        final RamQuota ramQuota = ( RamQuota ) o;

        if ( ramQuotaValue != ramQuota.ramQuotaValue )
        {
            return false;
        }
        if ( ramQuotaUnit != ramQuota.ramQuotaUnit )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result = ramQuotaUnit != null ? ramQuotaUnit.hashCode() : 0;
        result = 31 * result + ramQuotaValue;
        return result;
    }


    public String getQuotaValue()
    {
        return String.format( "%d%s", ramQuotaValue, ramQuotaUnit.getAcronym() );
    }
}
