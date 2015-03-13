package org.safehaus.subutai.core.peer.cli;


import org.safehaus.subutai.common.peer.PeerInfo;
import org.safehaus.subutai.common.util.UUIDUtil;
import org.safehaus.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import org.safehaus.subutai.core.peer.api.PeerManager;

import org.apache.karaf.shell.commands.Command;


/**
 * Created by bahadyr on 8/28/14.
 */
@Command( scope = "peer", name = "register" )
public class RegisterCommand extends SubutaiShellCommandSupport
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
        PeerInfo peerInfo = getSamplePeer();

        if ( peerManager.register( peerInfo ) )
        {
            System.out.println( "Peer registered." );
        }
        else
        {
            System.out.println( "Failed to register peer." );
        }

        return null;
    }


    private PeerInfo getSamplePeer()
    {
        PeerInfo peerInfo = new PeerInfo();
        peerInfo.setName( "Peer name" );
        peerInfo.setIp( "10.10.10.10" );
        peerInfo.setId( UUIDUtil.generateTimeBasedUUID() );
        return peerInfo;
    }
}
