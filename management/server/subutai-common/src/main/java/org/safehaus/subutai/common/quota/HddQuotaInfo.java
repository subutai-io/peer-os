package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 12/2/14.
 */
public class HddQuotaInfo extends QuotaInfo
{
    private DiskPartition partition;
    private Memory memory;


    public HddQuotaInfo( final DiskPartition partition, final String memory )
    {
        this.partition = partition;
        this.memory = new Memory( memory );
    }


    public DiskPartition getPartition()
    {
        return partition;
    }


    public Memory getMemory()
    {
        return memory;
    }


    @Override
    public String getQuotaKey()
    {
        return partition.getPartitionName();
    }


    @Override
    public String getQuotaValue()
    {
        return memory.toString();
    }
}
