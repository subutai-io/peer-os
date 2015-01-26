package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 10/7/14.
 */

public enum QuotaType
{
    QUOTA_TYPE_CPU( "cpu" ),
    QUOTA_TYPE_DISK_HOME( DiskPartition.HOME.getPartitionName() ),
    QUOTA_TYPE_DISK_VAR( DiskPartition.VAR.getPartitionName() ),
    QUOTA_TYPE_DISK_OPT( DiskPartition.OPT.getPartitionName() ),
    QUOTA_TYPE_DISK_ROOTFS( DiskPartition.ROOT_FS.getPartitionName() ),
    QUOTA_TYPE_RAM( "ram" ),
    QUOTA_TYPE_ALL_JSON( "json" );

    private String key;


    private QuotaType( String key )
    {
        this.key = key;
    }


    public String getKey()
    {
        return key;
    }


    public static QuotaType getQuotaType( String quotaType )
    {
        if ( "cpu".equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_CPU;
        }
        else if ( DiskPartition.HOME.getPartitionName().equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_DISK_HOME;
        }
        else if ( DiskPartition.VAR.getPartitionName().equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_DISK_VAR;
        }
        else if ( DiskPartition.OPT.getPartitionName().equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_DISK_OPT;
        }
        else if ( DiskPartition.ROOT_FS.getPartitionName().equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_DISK_ROOTFS;
        }
        else if ( "ram".equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_RAM;
        }
        else if ( "json".equalsIgnoreCase( quotaType ) )
        {
            return QUOTA_TYPE_ALL_JSON;
        }
        else
        {
            return null;
        }
    }
}
