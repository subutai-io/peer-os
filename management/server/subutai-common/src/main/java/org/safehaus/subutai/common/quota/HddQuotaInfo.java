package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/2/14.
 */
public class HddQuotaInfo extends QuotaInfo
{
    private String partitionName;
    private Memory memory;


    public HddQuotaInfo( final String partitionName, final String memory )
    {
        this.partitionName = partitionName;
        this.memory = new Memory( memory );
    }


    public String getPartitionName()
    {
        return partitionName;
    }


    public Memory getMemory()
    {
        return memory;
    }


    @Override
    public String getQuotaKey()
    {
        return "hdd.quota." + partitionName;
    }


    @Override
    public String getQuotaValue()
    {
        return memory.toString();
    }
}
