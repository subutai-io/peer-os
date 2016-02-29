package io.subutai.core.localpeer.cli;


import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.task.Task;
import io.subutai.common.util.StringUtil;
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
        for ( Task task : localPeer.getTaskList() )
        {
            final String s = task.getCommandBatch().asChain();
            System.out.println(
                    String.format( "%d\t%s\t%d\t%s\t%s...\t%s", task.getId(), task.getRequest().getResourceHostId(),
                            task.getTimeout(), task.getState(), s.substring( 0, Math.min( 25, s.length() ) ),
                            StringUtil.convertMillisToHHMMSS( task.getElapsedTime() ) ) );
        }
        return null;
    }
}
