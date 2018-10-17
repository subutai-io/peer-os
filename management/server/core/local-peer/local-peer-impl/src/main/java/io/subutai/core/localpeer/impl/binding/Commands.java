package io.subutai.core.localpeer.impl.binding;


import io.subutai.common.command.RequestBuilder;
import io.subutai.common.settings.Common;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;
import io.subutai.bazaar.share.quota.ContainerQuota;
import io.subutai.bazaar.share.quota.Quota;
import io.subutai.bazaar.share.resource.ByteUnit;
import io.subutai.bazaar.share.resource.ContainerResourceType;


/**
 * Subutai command bindings
 */
public abstract class Commands
{
    private static final String QUOTA_BINDING = "subutai quota";


    public static RequestBuilder getReadQuotaCommand( String containerName,
                                                      ContainerResourceType containerResourceType )
    {
        return new RequestBuilder( QUOTA_BINDING ).withCmdArgs( containerName, containerResourceType.getKey() );
    }


    public static RequestBuilder getReadQuotaCommand( String containerName )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( ContainerResourceType resourceType : ContainerResourceType.values() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( "get" );
            quotaCommand.addArgument( "-r" );
            quotaCommand.addArgument( resourceType.getKey() );
            quotaCommand.addArgument( "-c" );
            quotaCommand.addArgument( containerName );
            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() ).withTimeout( Common.GET_QUOTA_TIMEOUT_SEC );
    }


    public static RequestBuilder getSetQuotaCommand( String containerName, ContainerQuota quota )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( Quota r : quota.getAll() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( "set" );
            quotaCommand.addArgument( "-r" );
            quotaCommand.addArgument( r.getResource().getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-c" );
            quotaCommand.addArgument( containerName );

            if ( r.getResource().getContainerResourceType() == ContainerResourceType.DISK )
            {
                //temp workaround for btrfs quota issue https://github.com/subutai-io/agent/wiki/Switch-to-Soft-Quota
                quotaCommand.addArgument( String.valueOf( r.getAsDiskResource().longValue( ByteUnit.GB ) * 2 ) );
            }
            else
            {
                quotaCommand.addArgument( r.getResource().getWriteValue() );
            }

            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() ).withTimeout( Common.SET_QUOTA_TIMEOUT_SEC );
    }
}
