package org.safehaus.subutai.common.quota;


public class HddQuotaInfo
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


    public String getQuotaKey()
    {
        return partition.getPartitionName();
    }


    public String getQuotaValue()
    {
        return memory.toString();
    }
}
