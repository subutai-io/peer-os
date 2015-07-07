package io.subutai.core.lxc.quota.impl;


import org.safehaus.subutai.common.command.RequestBuilder;

import com.google.common.collect.Lists;


/**
 * Quota manager commands
 */
public class Commands
{
    private static final String QUOTA_BINDING = "subutai quota";


    public RequestBuilder getReadRamQuotaCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerHostname, "ram" ) );
    }


    public RequestBuilder getWriteRamQuotaCommand( String containerHostname, int ramInMb )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, "ram", "-s", String.valueOf( ramInMb ) ) );
    }


    public RequestBuilder getWriteRamQuotaCommand2( String containerHostname, String ramQuota )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, "ram", "-s", ramQuota ) );
    }


    public RequestBuilder getReadCpuQuotaCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerHostname, "cpu" ) );
    }


    public RequestBuilder getWriteCpuQuotaCommand( String containerHostname, int cpuPercent )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, "cpu", "-s", String.valueOf( cpuPercent ) ) );
    }


    public RequestBuilder getReadCpuSetCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerHostname, "cpuset" ) );
    }


    public RequestBuilder getWriteCpuSetCommand( String containerHostname, String cpuset )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, "cpuset", "-s", cpuset ) );
    }


    public RequestBuilder getReadDiskQuotaCommand( String containerHostname, String diskPartition )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, diskPartition ) );
    }


    public RequestBuilder getWriteDiskQuotaCommand( String containerHostname, String diskPartition, String diskQuota )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, diskPartition, "-s", diskQuota ) );
    }


    public RequestBuilder getReadAvailableDiskQuotaCommand( String containerHostname, String diskPartition )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, diskPartition, "-m" ) );
    }


    public RequestBuilder getReadAvailableRamQuotaCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerHostname, "ram", "-m" ) );
    }


    public RequestBuilder getReadAvailableCpuQuotaCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerHostname, "cpu", "-m" ) );
    }
}
