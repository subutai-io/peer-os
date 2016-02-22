package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;
import io.subutai.common.task.Task;


@Command( scope = "task", name = "list" )
public class TaskListCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;


    public TaskListCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        for ( Task task : localPeer.getTaskList() )
        {
            System.out.println(
                    String.format( "%s\t%s\t%d\t%s", task.getHost().getHostname(), task.getState(), task.getTimeout(),
                            task.getCommandBatch().asJson() ) );
        }
        return null;
    }
}
