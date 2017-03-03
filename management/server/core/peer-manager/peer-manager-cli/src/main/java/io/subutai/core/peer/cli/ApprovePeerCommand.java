package io.subutai.core.peer.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.RegistrationData;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "approve" )
public class ApprovePeerCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;

    @Argument( name = "peer id", required = true, description = "peer identifier" )
    private String peerId;
    @Argument( index = 1, name = "keyphrase", required = true, description = "key phrase" )
    private String keyPhrase;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
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
            peerManager.doApproveRequest( keyPhrase, request );
        }
        else
        {
            System.out.println( "Registration request not found." );
        }
        return null;
    }
}
