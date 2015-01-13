package org.safehaus.subutai.core.lxc.quota.impl;


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
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( "ram", containerHostname ) );
    }


    public RequestBuilder getWriteRamQuotaCommand( String containerHostname, int ramInMb )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( "ram", containerHostname, String.valueOf( ramInMb ) ) );
    }


    public RequestBuilder getReadCpuQuotaCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( "cpu", containerHostname ) );
    }


    public RequestBuilder getWriteCpuQuotaCommand( String containerHostname, int cpuPercent )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( "cpu", containerHostname, String.valueOf( cpuPercent ) ) );
    }


    public RequestBuilder getReadCpuSetCommand( String containerHostname )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( "cpuset", containerHostname ) );
    }


    public RequestBuilder getWriteCpuSetCommand( String containerHostname, String cpuset )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( "cpuset", containerHostname, cpuset ) );
    }
}
