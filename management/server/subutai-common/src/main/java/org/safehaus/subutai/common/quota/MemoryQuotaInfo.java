package org.safehaus.subutai.common.quota;


import com.google.common.base.Preconditions;


public class MemoryQuotaInfo extends QuotaInfo
{
    private DiskQuotaUnit quotaUnit;
    private double memoryQuotaValue;


    public MemoryQuotaInfo()
    {
    }


    public MemoryQuotaInfo( final DiskQuotaUnit quotaUnit, final Double memoryQuotaValue )
    {
        Preconditions.checkNotNull( quotaUnit, "Quota Unit cannot be null" );
        Preconditions.checkNotNull( memoryQuotaValue, "Memory Quota value cannot be null" );
        this.quotaUnit = quotaUnit;
        this.memoryQuotaValue = memoryQuotaValue;
    }


    public DiskQuotaUnit getQuotaUnit()
    {
        return quotaUnit;
    }


    public double getMemoryQuota()
    {
        return memoryQuotaValue;
    }


    @Override
    public String getQuotaKey()
    {
        return QuotaType.QUOTA_TYPE_RAM.getKey();
    }


    @Override
    public String getQuotaValue()
    {
        return String.format( "%.1f", memoryQuotaValue );
    }


    @Override
    public QuotaType getQuotaType()
    {
        return QuotaType.QUOTA_TYPE_RAM;
    }
}
