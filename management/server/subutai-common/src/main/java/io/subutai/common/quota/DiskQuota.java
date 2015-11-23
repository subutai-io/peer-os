package io.subutai.common.quota;


import java.math.BigDecimal;


/**
 * Disk quota
 */
public class DiskQuota extends Quota
{

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


    public String getKey()
    {
        return diskPartition.getPartitionName();
    }


    public String getValue()
    {
        if ( diskQuotaUnit == DiskQuotaUnit.UNLIMITED )
        {
            return DiskQuotaUnit.UNLIMITED.getAcronym();
        }
        return String.format( "%.2f%s", diskQuotaValue, diskQuotaUnit.getAcronym() );
    }


    public BigDecimal getValue( DiskQuotaUnit unit )
    {
        BigDecimal inBytes = diskQuotaUnit.getMultiplicator().multiply( new BigDecimal( diskQuotaValue ) );
        return inBytes.divide( unit.getMultiplicator() );
    }


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
