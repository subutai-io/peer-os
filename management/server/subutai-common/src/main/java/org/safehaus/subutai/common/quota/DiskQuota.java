package org.safehaus.subutai.common.quota;


/**
 * Disk quota
 */
public class DiskQuota extends QuotaInfo
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
}
