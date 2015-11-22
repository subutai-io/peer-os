package io.subutai.common.quota;


public class CpuQuota extends Quota
{
    private int percentage;


    public CpuQuota( int cpuAmount )
    {
        this.percentage = cpuAmount;
    }


    public int getPercentage()
    {
        return percentage;
    }


    public void setPercentage( final int percentage )
    {
        this.percentage = percentage;
    }


    @Override
    public String getValue()
    {
        return String.format( "%d", percentage );
    }


    @Override
    public String getKey()
    {
        return QuotaType.QUOTA_TYPE_CPU.getKey();
    }


    public QuotaType getType()
    {
        return QuotaType.QUOTA_TYPE_CPU;
    }


    @Override
    public boolean equals( final Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof CpuQuota ) )
        {
            return false;
        }

        final CpuQuota that = ( CpuQuota ) o;

        if ( percentage != that.percentage )
        {
            return false;
        }

        return true;
    }


    @Override
    public int hashCode()
    {
        return percentage;
    }

}
