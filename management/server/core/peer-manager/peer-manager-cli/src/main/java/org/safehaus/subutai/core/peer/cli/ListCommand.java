package org.safehaus.subutai.core.peer.cli;


import java.util.List;

import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "ls")
public class ListCommand extends OsgiCommandSupport
{

    private PeerManager peerManager;


    public PeerManager getPeerManager()
    {
        return peerManager;
    }


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Peer> list = peerManager.getPeers();
        System.out.println( "Found " + list.size() + " registered peers" );
        for ( Peer peer : list )
        {
            String peerStatus = "OFFLINE";
            try
            {

                if ( peer.isOnline() )
                {
                    peerStatus = "ONLINE";
                }
            }
            catch ( PeerException pe )
            {
                peerStatus += " "+pe.toString();
            }
            System.out.println(
                    peer.getId() + " " + peer.getPeerInfo().getIp() + " " + peer.getName() + " " + peerStatus );
        }
        return null;
    }
}
