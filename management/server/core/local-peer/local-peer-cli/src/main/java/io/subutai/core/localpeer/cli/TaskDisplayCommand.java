package io.subutai.core.localpeer.cli;


import org.apache.commons.lang.StringUtils;
import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.task.Task;
import io.subutai.core.identity.rbac.cli.SubutaiShellCommandSupport;


@Command( scope = "task", name = "display" )
public class TaskDisplayCommand extends SubutaiShellCommandSupport
{

    private LocalPeer localPeer;

    @Argument( index = 0, name = "id", multiValued = false, required = true, description = "Task ID" )
    private Integer taskId;


    public TaskDisplayCommand( final LocalPeer localPeer )
    {
        this.localPeer = localPeer;
    }


    public void setTaskId( final Integer taskId )
    {
        this.taskId = taskId;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        Task task = localPeer.getTask( taskId );

        if ( task == null )
        {
            System.out.println( "Task not found." );
            return null;
        }

        if ( !task.isDone() )
        {
            System.out.println( "Task is running. Please try later." );
            return null;
        }

        final String s = task.getCommandBatch().toString();
        System.out.println(
                String.format( "%s\t%d\t%s...\t%s", task.getRequest().getResourceHostId(), task.getTimeout(),
                        s.substring( 0, Math.min( 50, s.length() ) ), task.getState() ) );

        System.out.println( String.format( "Request: %s", task.getRequest() ) );
        System.out.println( String.format( "Response: %s", task.waitAndGetResponse() ) );
        System.out.println( "ExitCode: " + task.getExitCode() );
        System.out.println( "StdOut: " + task.getStdErr() );
        System.out.println( "StdErr: " + task.getStdErr() );
        final String exceptions = task.getExceptionsAsString();

        if ( StringUtils.isNotBlank( exceptions ) )
        {
            System.out.println( "Exceptions:\n" + exceptions );
        }

        return null;
    }
}
