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
import io.subutai.common.task.ResponseCollector;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskResponse;


public class TaskExecutor implements Callable<Task>
{
    private static final Logger LOG = LoggerFactory.getLogger( TaskExecutor.class );
    private LocalPeer localPeer;
    private Task task;
    private int taskId;
    private ResponseCollector collector;
    protected CommandUtil commandUtil = new CommandUtil();


    public TaskExecutor( final LocalPeer localPeer, final Task task, final int taskId, ResponseCollector collector )
    {
        this.localPeer = localPeer;
        this.task = task;
        this.taskId = taskId;
        this.collector = collector;
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
        if ( collector != null )
        {
            TaskResponse response = task.waitAndGetResponse();
            collector.onResponse( response );
        }

        return task;
    }
}
