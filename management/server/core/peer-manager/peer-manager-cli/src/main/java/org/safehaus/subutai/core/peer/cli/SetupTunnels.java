package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.collect.Sets;


@Command( scope = "peer", name = "setup-tunnels" )
public class SetupTunnels extends OsgiCommandSupport
{
    private PeerManager peerManager;

    @Argument( index = 0, name = "peerId", multiValued = false, required = true, description = "Peer ID" )
    private String peerId;
    @Argument( index = 1, name = "vni", multiValued = false, required = true, description = "VNI" )
    private long vni;
    @Argument( index = 2, name = "remotePeerIP", multiValued = false, required = true, description = "Remote peer IP" )
    private String remotePeerIp;
    @Argument( index = 3, name = "newVni", multiValued = false, description = "New VNI" )
    private boolean newVni = true;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Peer peer = peerManager.getPeer( peerId );

        peer.setupTunnels( Sets.newHashSet( remotePeerIp ), vni, newVni );

        return null;
    }
}
