package org.safehaus.subutai.core.peer.cli;


import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.UUID;

import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.ManagementHost;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.ResourceHost;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "hosts" )
public class HostsCommand extends OsgiCommandSupport
{

    DateFormat fmt = new SimpleDateFormat( "dd.MM.yy HH:mm:ss.SS" );
    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        LocalPeer localPeer = peerManager.getLocalPeer();
        //        localPeer.init();
        ManagementHost managementHost = localPeer.getManagementHost();
        if ( managementHost == null )
        {
            System.out.println( "Management host not available." );
            return null;
        }

        System.out.println( "Time:" + fmt.format( System.currentTimeMillis() ) );
        System.out.println( "List of hosts in local peer:" );
        print( managementHost, "" );
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


    private void print( Host host, String padding ) throws PeerException
    {
        String lastHeartbeat = fmt.format( host.getLastHeartbeat() );
        String containerInfo =
                String.format( "%s\t(%s) ", host.isConnected() ? " CONNECTED" : " DISCONNECTED", lastHeartbeat );
        if ( host instanceof ContainerHost )
        {
            ContainerHost c = ( ContainerHost ) host;
            containerInfo += c.getState();
            if ( c.getEnvironmentId() != null )
            {
                containerInfo += " " + c.getEnvironmentId();
            }

            if ( c.getCreatorPeerId() != null )
            {
                Peer peer = peerManager.getPeer( UUID.fromString( c.getCreatorPeerId() ) );
                if ( peer != null )
                {
                    containerInfo += " " + peer.getPeerInfo().getIp();
                }
            }
        }

        System.out
                .println( String.format( "%s+--%s %s %s", padding, host.getHostname(), host.getId(), containerInfo ) );
    }
}
