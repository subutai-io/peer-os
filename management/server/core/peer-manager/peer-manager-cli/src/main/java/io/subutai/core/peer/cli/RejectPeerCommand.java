package io.subutai.core.peer.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.RegistrationData;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "reject" )
public class RejectPeerCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( index = 0, name = "peer id", required = true, multiValued = false,
            description = "peer identifier" )
    private String peerId;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    public void setPeerId( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        final List<RegistrationData> requests = peerManager.getRegistrationRequests();
        RegistrationData request = null;
        for ( int i = 0; i < requests.size() && request == null; i++ )
        {
            if ( requests.get( i ).getPeerInfo().getId().equals( peerId ) )
            {
                request = requests.get( i );
            }
        }

        if ( request != null )
        {
            peerManager.doRejectRequest( request, true );
        }
        else
        {
            System.out.println( "Registration request not found." );
        }
        return null;
    }
}
