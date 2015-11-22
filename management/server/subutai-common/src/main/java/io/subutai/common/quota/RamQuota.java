package io.subutai.common.quota;


/**
 * RAM quota
 */
public class RamQuota extends Quota
{
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


    @Override
    public String getKey()
    {
        return QuotaType.QUOTA_TYPE_RAM.getKey();
    }


    @Override
    public QuotaType getType()
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

        final RamQuota ramQuotaInfo = ( RamQuota ) o;

        if ( ramQuotaValue != ramQuotaInfo.ramQuotaValue )
        {
            return false;
        }
        if ( ramQuotaUnit != ramQuotaInfo.ramQuotaUnit )
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


    public String getValue()
    {
        return String.format( "%d%s", ramQuotaValue, ramQuotaUnit.getAcronym() );
    }
}
