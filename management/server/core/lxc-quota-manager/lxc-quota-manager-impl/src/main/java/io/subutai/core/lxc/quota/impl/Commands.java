package io.subutai.core.lxc.quota.impl;


import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;


/**
 * Quota manager commands
 */
public class Commands
{
    private static final String QUOTA_BINDING = "subutai quota";


    public RequestBuilder getReadQuotaCommand( String containerHostname, ResourceType resourceType )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerHostname, resourceType.getKey() ) );
    }


    public RequestBuilder getWriteQuotaCommand( String containerHostname, ResourceType resourceType,
                                                ResourceValue resourceValue )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs(
                Lists.newArrayList( containerHostname, resourceType.getKey(), "-s",
                        resourceValue.getWriteValue( resourceType.getDefaultMeasureUnit() ) ) );
    }


    public RequestBuilder getReadAvailableQuotaCommand( final String containerName, final ResourceType resourceType )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, resourceType.getKey(), "-m" ) );
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
}
