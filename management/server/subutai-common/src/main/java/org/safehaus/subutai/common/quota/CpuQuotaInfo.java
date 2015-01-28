package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/2/14.
 */
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
        String cpuAmount = String.valueOf( percentage );
        if ( percentage == 0 )
        {
            cpuAmount = "none";
        }
        return cpuAmount;
    }


    @Override
    public String getQuotaKey()
    {
        return "cpu";
    }
}
