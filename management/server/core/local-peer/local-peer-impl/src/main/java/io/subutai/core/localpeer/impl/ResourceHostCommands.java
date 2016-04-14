package io.subutai.core.localpeer.impl;


import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.quota.ContainerQuota;
import io.subutai.common.quota.Quota;
import io.subutai.common.settings.Common;
import io.subutai.common.task.Command;
import io.subutai.common.task.CommandBatch;


public class ResourceHostCommands
{
    public RequestBuilder getListContainerInfoCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai list -i %s", hostname ) );
    }


    public RequestBuilder getStartContainerCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai start %s", hostname ) ).withTimeout( 1 ).daemon();
    }


    public RequestBuilder getStopContainerCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai stop %s", hostname ) ).withTimeout( 120 );
    }


    public RequestBuilder getDestroyContainerCommand( String hostname )
    {
        return new RequestBuilder( String.format( "subutai destroy %s", hostname ) ).withTimeout( 60 );
    }


    public RequestBuilder getCleanupEnvironmentCommand( int vlan )
    {
        return new RequestBuilder( String.format( "subutai cleanup %d", vlan ) ).withTimeout( 60 * 60 );
    }


    public RequestBuilder getFetchCpuCoresNumberCommand()
    {
        return new RequestBuilder( "nproc" );
    }


    public RequestBuilder getImportTemplateCommand( final String templateName )
    {
        return new RequestBuilder( String.format( "subutai import %s", templateName ) )
                .withTimeout( Common.TEMPLATE_DOWNLOAD_TIMEOUT_SEC );
    }


    public RequestBuilder getCloneContainerCommand( final String templateName, String hostName, String ip, int vlan,
                                                    String environmentId, String token )
    {
        return new RequestBuilder( "subutai clone" ).withCmdArgs(
                Lists.newArrayList( templateName, hostName, "-i", String.format( "\"%s %d\"", ip, vlan ), "-e",
                        environmentId, "-t", token ) ).withTimeout( Common.CLONE_TIMEOUT_SEC );
    }


    public RequestBuilder getSetQuotaCommand( String hostname, ContainerQuota quota )
    {

        CommandBatch result = new CommandBatch( CommandBatch.Type.JSON );

        for ( Quota r : quota.getAll() )
        {

            Command quotaCommand = new Command( "quota" );
            quotaCommand.addArgument( hostname );
            quotaCommand.addArgument( r.getResource().getContainerResourceType().getKey() );
            quotaCommand.addArgument( "-s" );
            quotaCommand.addArgument( r.getResource().getWriteValue() );
            result.addCommand( quotaCommand );
        }

        return new RequestBuilder( result.toString() );
    }
}
