package org.safehaus.subutai.core.monitor.api;


public enum Metric
{

    BYTES_IN( "Bytes Received", "bytes/sec" ),
    BYTES_OUT( "Bytes Sent", "bytes/sec" ),
    CPU_USER( "CPU User", "%" ),
    CPU_SYSTEM( "CPU System", "%" ),
    CPU_IDLE( "CPU Idle", "%" ),
    CPU_AIDLE( "CPU AIdle", "%" ),
    CPU_NICE( "CPU Nice", "%" ),
    CPU_WIO( "CPU WIO", "%" ),
    MEM_FREE( "Free Memory", "KB" ),
    MEM_CACHED( "Cached Memory", "KB" ),
    MEM_BUFFERS( "Memory Buffers", "KB" ),
    MEM_SHARED( "Memory Shared", "KB" ),
    MEM_TOTAL( "Memory Total", "KB" ),
    PART_MAX_USED( "Maximum Disk Space Used", "%" ),
    PKTS_IN( "Packets Received", "packets/sec" ),
    PKTS_OUT( "Packets Sent", "packets/sec" ),
    SWAP_FREE( "Free Swap Space", "KB" ),
    SWAP_TOTAL( "Total Swap Space", "KB" ),
    LOAD_ONE( "Load One Minute", "" ),
    LOAD_FIVE( "Load Five Minute", "" ),
    LOAD_FIFTEEN( "Load Fifteen Minute", "" ),
    //this metrics have different format
    DISK_OCTETS( "Disk octets", "" ),
    DISK_MERGED( "Disk merged", "" ),
    DISK_TIME( "Disk time", "" ),
    DISK_OPS( "Disk operations", "" );

    private String unit = "";
    private String description = "";


    private Metric( String description, String unit )
    {
        this.description = description;
        this.unit = unit;
    }


    public String getUnit()
    {
        return unit;
    }


    @Override
    public String toString()
    {
        return description;
    }
}
