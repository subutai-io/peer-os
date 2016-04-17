package io.subutai.core.localpeer.cli;


import java.util.Set;

import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.util.HostUtil;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


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
        Set<HostUtil.Task> tasks = localPeer.getTasks();

        System.out.format( "Found %d task(s)%n", tasks.size() );

        for ( HostUtil.Task task : tasks )
        {
            System.out.format( "%s on %s. Duration: %s. State: %s. Result: %s%n", task.name(),
                    task.getHost().getHostname(), task.getDurationFormatted(), task.getTaskState(),
                    task.getResult() == null ? task.getFailureReason() : task.getResult() );
        }

        return null;
    }
}
