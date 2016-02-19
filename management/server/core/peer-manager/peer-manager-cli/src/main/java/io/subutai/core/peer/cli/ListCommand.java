package io.subutai.core.peer.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.host.HostInterface;
import io.subutai.common.host.HostInterfaces;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerInfo;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "ls" )
public class ListCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Peer> list = peerManager.getPeers();
        System.out.println( "Found " + list.size() + " registered peer(s)" );
        if ( list.size() > 0 )
        {
            System.out.println( "Status\tL/R\tID\tOwner ID\tHost name\tPeer name\tPublic URL" );

            for ( Peer peer : list )
            {
                String peerStatus = "OFFLINE";


                if ( peer.isOnline() )
                {
                    peerStatus = "ONLINE";
                }


                try
                {
                    PeerInfo info = peer.getPeerInfo();
                    System.out.println( String.format( "%s\t%s\t%s\t%s\t%s\t%s\t%s", peerStatus,
                                    peer.isLocal() ? "local" : "remote", peer.getId(), info.getOwnerId(), info.getIp(),
                                    info.getName(), info.getPublicUrl() ) );
                }
                catch ( Exception e )
                {
                    log.error( e.getMessage(), e );
                }
            }
        }
        return null;
    }
}
