package org.safehaus.subutai.common.quota;


/**
 * Disk quota
 */
public class DiskQuota
{
    private DiskPartition diskPartition;
    private DiskQuotaUnit diskQuotaUnit;
    private long diskQuotaValue;


    public DiskQuota( final DiskPartition diskPartition, final DiskQuotaUnit diskQuotaUnit, final long diskQuotaValue )
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


    public long getDiskQuotaValue()
    {
        return diskQuotaValue;
    }
}
