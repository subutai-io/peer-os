package org.safehaus.subutai.core.monitor.api;


public enum Metric
{

    CPU_USER( "CPU User", "%" ),
    CPU_SYSTEM( "CPU System", "%" ),
    CPU_IDLE( "CPU Idle", "%" ),
    CPU_WIO( "CPU wio", "%" ),
    MEM_FREE( "Free Memory", "KB" ),
    DISK_OPS( "Disk operations", "KB" ),
    MEM_CACHED( "Cached Memory", "KB" ),
    MEM_BUFFERS( "Memory Buffers", "KB" ),
    SWAP_FREE( "Free Swap Space", "KB" ),
    PKTS_IN( "Packets Received", "packets/sec" ),
    PKTS_OUT( "Packets Sent", "packets/sec" ),
    BYTES_IN( "Bytes Received", "bytes/sec" ),
    BYTES_OUT( "Bytes Sent", "bytes/sec" ),
    PART_MAX_USED( "Maximum Disk Space Used", "%" );

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
