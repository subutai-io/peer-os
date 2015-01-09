package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.core.peer.api.HostTask;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;


@Command( scope = "peer", name = "tasks" )
public class TaskCommand extends OsgiCommandSupport
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
        for ( HostTask hostTask : localPeer.getTasks() )
        {
            System.out.println( String.format( "Task %s %s on %s", hostTask.getId(), hostTask.getPhase(),
                    hostTask.getHost().getHostname() ) );
        }
        return null;
    }
}
