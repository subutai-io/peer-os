package org.safehaus.subutai.core.monitor.api;


public enum MetricType
{
    BYTES_IN( "Bytes Received" ),
    BYTES_OUT( "Bytes Sent" ),
    CPU_USER( "CPU User" ),
    CPU_SYSTEM( "CPU System" ),
    CPU_IDLE( "CPU Idle" ),
    CPU_AIDLE( "CPU AIdle" ),
    CPU_NICE( "CPU Nice" ),
    CPU_WIO( "CPU WIO" ),
    MEM_FREE( "Free Memory" ),
    MEM_CACHED( "Cached Memory" ),
    MEM_BUFFERS( "Memory Buffers" ),
    MEM_SHARED( "Memory Shared" ),
    MEM_TOTAL( "Memory Total" ),
    PART_MAX_USED( "Maximum Disk Space Used" ),
    PKTS_IN( "Packets Received" ),
    PKTS_OUT( "Packets Sent" ),
    SWAP_FREE( "Free Swap Space" ),
    SWAP_TOTAL( "Total Swap Space" ),
    LOAD_ONE( "Load One Minute" ),
    LOAD_FIVE( "Load Five Minute" ),
    LOAD_FIFTEEN( "Load Fifteen Minute" ),

    //this metrics have different format
    DISK_OCTETS( "Disk octets", PluginType.DISK ),
    DISK_MERGED( "Disk merged", PluginType.DISK ),
    DISK_TIME( "Disk time", PluginType.DISK ),
    DISK_OPS( "Disk operations", PluginType.DISK );


    private PluginType pluginType = PluginType.DEFAULT;
    private String description = "";


    private MetricType( String description, PluginType pluginType )
    {
        this.description = description;
        this.pluginType = pluginType;
    }


    MetricType( final String description )
    {
        this.description = description;
    }


    public PluginType getPluginType()
    {
        return pluginType;
    }


    @Override
    public String toString()
    {
        return description;
    }
}
