package org.safehaus.subutai.core.peer.cli;


import java.util.UUID;

import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command(scope = "peer", name = "id")
public class GetIdCommand extends OsgiCommandSupport
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
        UUID id = peerManager.getSiteId();
        System.out.println( "SUBUTAI ID: " + id );
        return null;
    }
}
