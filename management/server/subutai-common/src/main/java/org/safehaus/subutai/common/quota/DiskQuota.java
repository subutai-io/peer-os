package org.safehaus.subutai.common.quota;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Disk quota
 */
public class DiskQuota extends QuotaInfo
{
    private static final String QUOTA_REGEX = "(\\d+(?:[\\.,]\\d+)?)(K|M|G|T|P|E)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private DiskPartition diskPartition;
    private DiskQuotaUnit diskQuotaUnit;
    private double diskQuotaValue;


    public DiskQuota( final DiskPartition diskPartition, final DiskQuotaUnit diskQuotaUnit,
                      final double diskQuotaValue )
    {
        this.diskPartition = diskPartition;
        this.diskQuotaUnit = diskQuotaUnit;
        this.diskQuotaValue = diskQuotaValue;
    }


    public DiskPartition getDiskPartition()
    {
        return diskPartition;
    }


    public DiskQuotaUnit getDiskQuotaUnit()
    {
        return diskQuotaUnit;
    }


    public double getDiskQuotaValue()
    {
        return diskQuotaValue;
    }


    public String getQuotaKey()
    {
        return diskPartition.getPartitionName();
    }


    public String getQuotaValue()
    {
        if ( diskQuotaUnit == DiskQuotaUnit.UNLIMITED )
        {
            return DiskQuotaUnit.UNLIMITED.getAcronym();
        }
        return String.format( "%.2f%s", diskQuotaValue, diskQuotaUnit.getAcronym() );
    }


    public QuotaType getQuotaType()
    {
        return QuotaType.getQuotaType( diskPartition.getPartitionName() );
    }


    public static DiskQuota parse( DiskPartition diskPartition, String quotaString )
    {
        Preconditions.checkNotNull( diskPartition, "Invalid disk partition" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( quotaString ), "Invalid quota string" );

        if ( quotaString.contains( DiskQuotaUnit.UNLIMITED.getAcronym() ) )
        {
            return new DiskQuota( diskPartition, DiskQuotaUnit.UNLIMITED, -1 );
        }

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( quotaString.trim() );
        if ( quotaMatcher.matches() )
        {
            String quotaValue = quotaMatcher.group( 1 );
            double value = Double.parseDouble( quotaValue.replace( ",", "." ) );
            String acronym = quotaMatcher.group( 2 );
            DiskQuotaUnit diskQuotaUnit = DiskQuotaUnit.parseFromAcronym( acronym );
            return new DiskQuota( diskPartition, diskQuotaUnit == null ? DiskQuotaUnit.BYTE : diskQuotaUnit, value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Unparseable result: %s", quotaString ) );
        }
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof DiskQuota ) )
        {
            return false;
        }

        final DiskQuota diskQuota = ( DiskQuota ) o;

        if ( Double.compare( diskQuota.diskQuotaValue, diskQuotaValue ) != 0 )
        {
            return false;
        }
        if ( diskPartition != diskQuota.diskPartition )
        {
            return false;
        }
        if ( diskQuotaUnit != diskQuota.diskQuotaUnit )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = diskPartition != null ? diskPartition.hashCode() : 0;
        result = 31 * result + ( diskQuotaUnit != null ? diskQuotaUnit.hashCode() : 0 );
        temp = Double.doubleToLongBits( diskQuotaValue );
        result = 31 * result + ( int ) ( temp ^ ( temp >>> 32 ) );
        return result;
    }
}
