package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "localpeer", name = "add-ssh-key" )
public class AddSshKeyToUserCommand extends SubutaiShellCommandSupport
{
    private final LocalPeer localPeer;

    @Argument( index = 0, name = "container id", required = true, description = "target container id" )
    String containerId;

    @Argument( index = 1, name = "user", required = true, description = "username of target user" )
    String username;

    @Argument( index = 2, name = "ssh public key", required = true, description = "public ssh key to add" )
    String pubSshKey;


    public AddSshKeyToUserCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {

        localPeer.addAuthorizedSshKeyToUser( containerId, username, pubSshKey );

        return null;
    }
}
