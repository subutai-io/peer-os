package io.subutai.common.quota;


/**
 * Disk partition types for disk quotas
 */
public enum DiskPartition
{
    HOME( "diskHome" ),
    VAR( "diskVar" ),
    ROOT_FS( "diskRootfs" ),
    OPT( "diskOpt" );

    private String partitionName;


    private DiskPartition( final String partitionName )
    {
        this.partitionName = partitionName;
    }


    public String getPartitionName()
    {
        return partitionName;
    }
}
