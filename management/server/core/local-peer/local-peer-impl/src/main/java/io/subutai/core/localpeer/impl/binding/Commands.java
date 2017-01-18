package io.subutai.core.localpeer.impl.binding;


import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.hub.share.quota.ContainerQuota;
import io.subutai.hub.share.quota.Quota;
import io.subutai.hub.share.resource.ContainerResourceType;


/**
 * Subutai command bindings
 */
public abstract class Commands
{
    private static final String QUOTA_BINDING = "subutai quota";


    public static RequestBuilder getReadQuotaCommand( String containerName,
                                                      ContainerResourceType containerResourceType )
    {
        return new RequestBuilder( QUOTA_BINDING )
                .withCmdArgs( Lists.newArrayList( containerName, containerResourceType.getKey() ) );
    }


    public static RequestBuilder getReadQuotaCommand( String containerName )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( ContainerResourceType resourceType : ContainerResourceType.values() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( containerName );
            quotaCommand.addArgument( resourceType.getKey() );
            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() );
    }


    public static RequestBuilder getSetQuotaCommand( String containerName, ContainerQuota quota )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( Quota r : quota.getAll() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( containerName );
            quotaCommand.addArgument( r.getResource().getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getResource().getWriteValue() );
            if ( r.getThreshold() != null
                    && r.getResource().getContainerResourceType() != ContainerResourceType.CPUSET )
            {
                quotaCommand.addArgument( "-t" );
                quotaCommand.addArgument( r.getThreshold().toString() );
            }
            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() );
    }
}
