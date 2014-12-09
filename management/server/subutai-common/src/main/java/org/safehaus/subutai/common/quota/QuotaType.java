package org.safehaus.subutai.common.quota;


/**
 * Created by talas on 10/7/14.
 */

public enum QuotaType
{
    QUOTA_CPU_CPUS( "cpu.cpus" ),
    QUOTA_HDD_HOME( "hdd.quota.home" ),
    QUOTA_HDD_VAR( "hdd.quota.var" ),
    QUOTA_HDD_OPT( "hdd.quota.opt" ),
    QUOTA_HDD_ROOTFS( "hdd.quota.rootfs" ),
    QUOTA_MEMORY_QUOTA( "memory.quota" ),
    QUOTA_ALL_JSON( "json" );

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
        switch ( quotaType )
        {
            case "cpu.cpus":
                return QUOTA_CPU_CPUS;
            case "hdd.quota.home":
                return QUOTA_HDD_HOME;
            case "hdd.quota.var":
                return QUOTA_HDD_VAR;
            case "hdd.quota.opt":
                return QUOTA_HDD_OPT;
            case "hdd.quota.rootfs":
                return QUOTA_HDD_ROOTFS;
            case "memory.quota":
                return QUOTA_MEMORY_QUOTA;
            case "json":
                return QUOTA_ALL_JSON;
        }
        return null;
    }
}
