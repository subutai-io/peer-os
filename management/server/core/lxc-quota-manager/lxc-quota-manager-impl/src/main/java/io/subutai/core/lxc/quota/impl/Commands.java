package io.subutai.core.lxc.quota.impl;


import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.Quota;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Quota manager commands
 */
public class Commands
{
    private static final String QUOTA_BINDING = "subutai quota";


    public RequestBuilder getReadQuotaCommand( String containerName, ContainerResourceType containerResourceType )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, containerResourceType.getKey() ) );
    }


    public RequestBuilder getSetQuotaCommand( String containerName, ContainerQuota quota )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( Quota r : quota.getAll() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( containerName );
            quotaCommand.addArgument( r.getResource().getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getResource().getWriteValue() );
            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() );
    }


    public RequestBuilder getReadCpuSetCommand( String containerName )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( Lists.newArrayList( containerName, "cpuset" ) );
    }


    public RequestBuilder getWriteCpuSetCommand( String containerName, String cpuset )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, "cpuset", "-s", cpuset ) );
    }
}
