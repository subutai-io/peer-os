package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "hosts" )
public class HostsCommand extends OsgiCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        LocalPeer localPeer = peerManager.getLocalPeer();

        ManagementHost managementHost = localPeer.getManagementHost();


        System.out.println( "List of hosts in local peer:" );
        print( null, managementHost, "" );
        for ( ResourceHost resourceHost : managementHost.getResourceHosts() )
        {
            print( managementHost, resourceHost, "\t" );
            for ( ContainerHost containerHost : resourceHost.getContainerHosts() )
            {
                print( resourceHost, containerHost, "\t\t" );
            }
        }
        return null;
    }


    private void print( Host parentHost, Host host, String padding ) throws PeerException
    {
        String containerInfo = "";
        if ( host instanceof ContainerHost )
        {
            ContainerHost c = ( ContainerHost ) host;
            containerInfo += c.getEnvironmentId();
            containerInfo+=parentHost.isConnected( c ) ? " CONNECTED" : " DISCONNECTED";

            if ( c.getCreatorPeerId() != null )
            {
                containerInfo += peerManager.getPeerByUUID( c.getCreatorPeerId() ).getIp();
            }
        }

        System.out.println( String.format( "%s+--%s %s", padding, host.getHostname(), containerInfo ) );
    }
}
