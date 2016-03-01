package io.subutai.core.localpeer.impl;


import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.ResourceHost;
import io.subutai.common.task.Task;


public class TaskExecutor implements Callable<Task>
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskExecutor.class );
    private LocalPeer localPeer;
    private Task task;
    private int taskId;
    protected CommandUtil commandUtil = new CommandUtil();


    public TaskExecutor( final LocalPeer localPeer, final Task task, final int taskId )
    {
        this.localPeer = localPeer;
        this.task = task;
        this.taskId = taskId;
    }


    @Override
    public Task call() throws Exception
    {
        CommandResult commandResult;
        try
        {
            RequestBuilder builder = task.getRequestBuilder();
            final ResourceHost resourceHost = localPeer.getResourceHostById( task.getRequest().getResourceHostId() );

            task.start( taskId );
            LOG.debug( String.format( "Task %s started...", taskId ) );
            commandResult = commandUtil.execute( builder, resourceHost );
        }
        catch ( Exception e )
        {
            commandResult = new CommandResultImpl( -1, "", e.getMessage(), CommandStatus.FAILED );
        }
        LOG.debug( String.format( "Task %s finished", taskId ) );
        task.done( commandResult );
        return task;
    }
}
