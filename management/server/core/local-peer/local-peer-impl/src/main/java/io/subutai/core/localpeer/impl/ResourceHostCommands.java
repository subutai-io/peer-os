package io.subutai.core.localpeer.impl;


import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Timeouts;


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
                .withTimeout( Timeouts.PREPARE_TEMPLATES_RESPONSE_TIMEOUT );
    }
}
