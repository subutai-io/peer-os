package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "task", name = "cancel-all" )
public class TaskCancelAllCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public TaskCancelAllCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        localPeer.cancelAllTasks();

        System.out.println( "All tasks cancelled" );

        return null;
    }
}
