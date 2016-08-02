package io.subutai.core.localpeer.cli;


import org.apache.commons.lang3.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "peer", name = "hosts" )
public class HostsCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "abbreviate host ids", required = false, multiValued = false, description =
            "abbreviate host ids (true/false)" )
    boolean abbreviate = true;


    public HostsCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {


        System.out.println( "List of hosts in local peer:" );
        for ( ResourceHost resourceHost : localPeer.getResourceHosts() )
        {
            print( resourceHost, "\t" );
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                print( containerHost, "\t\t" );
            }
        }
        return null;
    }


    protected void print( Host host, String padding )
    {
        String connectionState = String.format( "%s ", host.isConnected() ? " CONNECTED" : " DISCONNECTED" );
        if ( host instanceof ContainerHost )
        {
            ContainerHost c = ( ContainerHost ) host;
            connectionState += c.getState();
        }

        System.out.println( String.format( "%s+--%s %s %s", padding, ( host instanceof ContainerHost ) ?
                                                                     host.getHostname() + " ["
                                                                             + ( ( ContainerHost ) host )
                                                                             .getContainerName() + "]" :
                                                                     host.getHostname(),
                abbreviate ? StringUtils.abbreviate( host.getId(), 7 ) : host.getId(), connectionState ) );
    }
}
