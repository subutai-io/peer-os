package io.subutai.core.localpeer.impl;


import com.google.common.collect.Lists;

import io.subutai.common.command.RequestBuilder;
import io.subutai.common.settings.Common;


public class ResourceHostCommands
{
    public RequestBuilder getListContainerInfoCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai list -i %s", containerName ) );
    }


    public RequestBuilder getStartContainerCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai start %s", containerName ) ).withTimeout( 1 ).daemon();
    }


    public RequestBuilder getStopContainerCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai stop %s", containerName ) ).withTimeout( 120 );
    }


    public RequestBuilder getDestroyContainerCommand( String containerName )
    {
        return new RequestBuilder( String.format( "subutai destroy %s", containerName ) ).withTimeout( 60 );
    }


    public RequestBuilder getCleanupEnvironmentCommand( int vlan )
    {
        return new RequestBuilder( String.format( "subutai cleanup %d", vlan ) ).withTimeout( 60 * 60 );
    }


    public RequestBuilder getFetchCpuCoresNumberCommand()
    {
        return new RequestBuilder( "nproc" );
    }


    public RequestBuilder getImportTemplateCommand( final String templateId )
    {
        return new RequestBuilder( String.format( "subutai import id:%s", templateId ) )
                .withTimeout( Common.TEMPLATE_DOWNLOAD_TIMEOUT_SEC );
    }


    public RequestBuilder getCloneContainerCommand( final String templateId, String containerName, String ip, int vlan,
                                                    String environmentId, String token )
    {
        return new RequestBuilder( "subutai clone" ).withCmdArgs(
                Lists.newArrayList( String.format( "id:%s", templateId ), containerName, "-i",
                        String.format( "\"%s %d\"", ip, vlan ), "-e", environmentId, "-t", token ) )
                                                    .withTimeout( Common.CLONE_TIMEOUT_SEC );
    }


    public RequestBuilder getGetRhVersionCommand()
    {
        return new RequestBuilder( "subutai -v" );
    }


    public RequestBuilder getGetVlanCommand()
    {
        return new RequestBuilder( "cat /var/lib/apps/subutai/current/vlan" );
    }


    public RequestBuilder getSetContainerHostnameCommand( final String containerName, final String newHostname )
    {
        return new RequestBuilder( String.format( "subutai hostname %s %s", containerName, newHostname ) );
    }


    public RequestBuilder getGetSetRhHostnameCommand( final String newHostname )
    {
        return new RequestBuilder( String.format( "subutai hostname %s ", newHostname ) );
    }
}
