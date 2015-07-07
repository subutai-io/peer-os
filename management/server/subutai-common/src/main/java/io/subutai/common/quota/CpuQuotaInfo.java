package io.subutai.common.quota;


public class CpuQuotaInfo extends QuotaInfo
{
    private int percentage;


    public CpuQuotaInfo( String cpuAmount )
    {
        this.percentage = Integer.parseInt( cpuAmount );
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
    public String getQuotaValue()
    {
        return String.format( "%d", percentage );
    }


    @Override
    public String getQuotaKey()
    {
        return QuotaType.QUOTA_TYPE_CPU.getKey();
    }


    public QuotaType getQuotaType()
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
        if ( !( o instanceof CpuQuotaInfo ) )
        {
            return false;
        }

        final CpuQuotaInfo that = ( CpuQuotaInfo ) o;

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
