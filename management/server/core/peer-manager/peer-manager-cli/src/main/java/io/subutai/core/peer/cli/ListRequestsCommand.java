package io.subutai.core.peer.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.RegistrationData;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "requests" )
public class ListRequestsCommand extends SubutaiShellCommandSupport
{

    private PeerManager peerManager;


    public void setPeerManager( final PeerManager peerManager )
    {
        this.peerManager = peerManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<RegistrationData> list = peerManager.getRegistrationRequests();
        System.out.println( "Found " + list.size() + " registration request(s)" );

        for ( RegistrationData peer : list )
        {
            System.out.printf( "%s\t%s\t%s\t%s\t%s%n", peer.getPeerInfo().getId(), peer.getPeerInfo().getName(),
                    peer.getPeerInfo().getIp(), peer.getKeyPhrase(), peer.getStatus() );
        }
        return null;
    }
}
