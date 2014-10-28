package org.safehaus.subutai.core.peer.cli;


import java.util.List;

import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command( scope = "peer", name = "ls" )
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
        List<PeerInfo> list = peerManager.peers();
        System.out.println( "Found " + list.size() + " registered peers" );
        for ( PeerInfo peerInfo : list )
        {
            System.out.println( peerInfo.getId() + " " + peerInfo.getIp() + " " + peerInfo.getName() );
        }
        return null;
    }
}
