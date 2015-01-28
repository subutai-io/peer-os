package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/2/14.
 */
public class MemoryQuotaInfo extends QuotaInfo
{
    private Memory memoryQuota;


    public MemoryQuotaInfo( final String memoryQuota )
    {
        this.memoryQuota = new Memory( memoryQuota );
        if ( this.memoryQuota.getValue() > Long.MAX_VALUE )
        {
            this.memoryQuota.unit = MemoryUnit.NONE;
        }
    }


    public MemoryQuotaInfo( final Memory memoryQuota )
    {
        this.memoryQuota = memoryQuota;
        if ( this.memoryQuota.getValue() > Long.MAX_VALUE )
        {
            this.memoryQuota.unit = MemoryUnit.NONE;
        }
    }


    public Memory getMemoryQuota()
    {
        return memoryQuota;
    }


    @Override
    public String getQuotaKey()
    {
        return "ram";
    }


    @Override
    public String getQuotaValue()
    {
        return memoryQuota.toString();
    }
}
