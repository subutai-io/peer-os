package io.subutai.core.localpeer.impl;


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


    public RequestBuilder getDestroyContainerCommand( String containerId )
    {
        return new RequestBuilder( String.format( "subutai destroy id:%s", containerId ) ).withTimeout( 60 );
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


    public RequestBuilder getCloneContainerCommand( final String templateId, String containerName, String hostname,
                                                    String ip, int vlan, String environmentId, String token )
    {
        return new RequestBuilder(
                String.format( "subutai clone id:%s %s -i \"%s %d\" -e %s -t %s && subutai hostname %s %s", templateId,
                        containerName, ip, vlan, environmentId, token, containerName, hostname ) )
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
        return new RequestBuilder( String.format( "hostnamectl set-hostname %s ", newHostname ) );
    }
}
