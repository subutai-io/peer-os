package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.subutai.common.task.ImportTemplateResponse;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;
import io.subutai.common.tracker.OperationMessage;
import io.subutai.common.util.StringUtil;


/**
 * Abstract group response
 */
public class AbstractGroupResponse<R extends TaskRequest, T extends TaskResponse>
{
    private final String peerId;
    private List<T> responses = new ArrayList<>();
    private List<Task<R, T>> tasks = new ArrayList<>();
    private AtomicInteger counter = new AtomicInteger( 0 );
    private boolean succeeded = true;
    private List<OperationMessage> messages = new ArrayList<>();


    public AbstractGroupResponse( final String peerId )
    {
        this.peerId = peerId;
    }


    public void addResponse( T response )
    {
        counter.incrementAndGet();
        if ( response == null )
        {
            addMessage( OperationMessage.Type.FAILED, "Task response empty. Something goes wrong." );
            succeeded = false;
            return;
        }
        responses.add( response );
        succeeded = succeeded && response.hasSucceeded();
        final String message = String.format( "%s [%s]", response.getLog(),
                StringUtil.convertMillisToHHMMSS( response.getElapsedTime() ) );
        if ( response.hasSucceeded() )
        {
            addMessage( OperationMessage.Type.SUCCEEDED, message );
        }
        else
        {
            addMessage( OperationMessage.Type.FAILED, message );
        }
    }


    public String getPeerId()
    {
        return peerId;
    }


    public List<T> getResponses()
    {
        return responses;
    }


    public List<OperationMessage> getOperationMessages()
    {
        if ( messages == null )
        {
            messages = new ArrayList<>();
        }
        return messages;
    }


    public void addMessage( OperationMessage.Type type, final String msg, final String description )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Fail message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, type, description ) );
    }


    public void addMessage( OperationMessage.Type type, final String msg )
    {
        if ( msg == null )
        {
            throw new IllegalArgumentException( "Fail message could not be null." );
        }

        this.messages.add( new OperationMessage( msg, type, null ) );
    }


    public void addTask( Task<R, T> task )
    {
        this.tasks.add( task );
    }


    public void waitResponses()
    {
        for ( Task<R, T> task : tasks )
        {
            T response = task.waitAndGetResponse();
            addResponse( response );
        }
    }


    public boolean hasSucceeded()
    {
        return succeeded;
    }
}
