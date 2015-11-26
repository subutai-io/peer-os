package io.subutai.common.quota;


import java.math.BigDecimal;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;


/**
 * Disk quota
 */
public class DiskQuota extends Quota
{
    @JsonProperty( "diskPartition" )
    private DiskPartition diskPartition;
    @JsonProperty( "diskQuotaUnit" )
    private DiskQuotaUnit diskQuotaUnit;
    @JsonProperty( "diskQuotaValue" )
    private double diskQuotaValue;


    public DiskQuota( @JsonProperty( "diskPartition" ) final DiskPartition diskPartition,
                      @JsonProperty( "diskQuotaUnit" ) final DiskQuotaUnit diskQuotaUnit,
                      @JsonProperty( "diskQuotaValue" ) final double diskQuotaValue )
    {
        this.diskPartition = diskPartition;
        this.diskQuotaUnit = diskQuotaUnit;
        this.diskQuotaValue = diskQuotaValue;
    }


    public DiskQuota( final DiskPartition partition )
    {
        this.diskPartition = partition;
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


    @JsonIgnore
    public String getKey()
    {
        return diskPartition.getPartitionName();
    }


    @JsonIgnore
    public String getValue()
    {
        if ( diskQuotaUnit == DiskQuotaUnit.UNLIMITED )
        {
            return DiskQuotaUnit.UNLIMITED.getAcronym();
        }
        return String.format( "%.2f%s", diskQuotaValue, diskQuotaUnit.getAcronym() );
    }


    @JsonIgnore

    public BigDecimal getValue( DiskQuotaUnit unit )
    {
        BigDecimal inBytes = diskQuotaUnit.getMultiplicator().multiply( new BigDecimal( diskQuotaValue ) );
        return inBytes.divide( unit.getMultiplicator() );
    }


    @JsonIgnore
    public QuotaType getType()
    {
        return QuotaType.getQuotaType( diskPartition.getPartitionName() );
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
