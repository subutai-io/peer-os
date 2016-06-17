package io.subutai.core.peer.cli;


import java.util.List;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.RegistrationData;
import io.subutai.common.peer.RegistrationStatus;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.core.peer.api.PeerManager;


@Command( scope = "peer", name = "status" )
public class RegistrationStatusCommand extends SubutaiShellCommandSupport
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
        final RegistrationStatus localStatus = peerManager.getRegistrationStatus( peerId );
        final RegistrationStatus remoteStatus = peerManager.getRemoteRegistrationStatus( peerId );

        System.out.println( "Local status\tRemoteStatus" );
        System.out.println( String.format( "%s\t%s", localStatus, remoteStatus ) );
        return null;
    }
}
