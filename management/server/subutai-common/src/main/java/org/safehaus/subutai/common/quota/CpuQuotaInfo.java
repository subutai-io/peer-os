package org.safehaus.subutai.common.quota;


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
}
