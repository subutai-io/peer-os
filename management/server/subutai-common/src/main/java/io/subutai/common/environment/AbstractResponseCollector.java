package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;

import io.subutai.common.task.ResponseCollector;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;
import io.subutai.common.tracker.OperationMessage;
import io.subutai.common.util.StringUtil;


/**
 * Abstract group response
 */
public class AbstractResponseCollector<R extends TaskRequest, T extends TaskResponse> implements ResponseCollector
{
    private final String peerId;
    private List<T> responses = new ArrayList<>();
    transient private List<Future<Task>> tasks = new ArrayList<>();
    private AtomicInteger counter = new AtomicInteger( 0 );
    private boolean succeeded = true;
    private List<OperationMessage> messages = new CopyOnWriteArrayList<>();


    public AbstractResponseCollector( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public void onResponse( final TaskResponse response )
    {
        counter.incrementAndGet();
        if ( response == null )
        {
            addMessage( OperationMessage.Type.FAILED, "Task response empty. Something goes wrong." );
            succeeded = false;
            return;
        }
        responses.add( ( T ) response );
        succeeded = succeeded && response.hasSucceeded();
        final String message = String.format( "%s [%s]", response.getLog(),
                StringUtil.convertMillisToHHMMSS( response.getElapsedTime() ) );
        final String description = StringUtils.isNotBlank( response.getDescription() ) ?
                                   String.format( "%s:%s [%s]", getPeerId(), response.getResourceHostId(),
                                           response.getDescription() ) : "";
        if ( response.hasSucceeded() )
        {
            addMessage( OperationMessage.Type.SUCCEEDED, message, description );
        }
        else
        {
            addMessage( OperationMessage.Type.FAILED, message, description );
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


    public void addTask( Future<Task> task )
    {
        this.tasks.add( task );
    }


    public void waitResponses()
    {
        int size = tasks.size();
        while ( counter.get() < size && succeeded )
        {
            try
            {
                TimeUnit.MILLISECONDS.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
                // ignore
            }
        }

        for ( Future f : tasks )
        {
            if ( !f.isDone() )
            {
                f.cancel( false );
            }
        }
    }


    public boolean hasSucceeded()
    {
        return succeeded;
    }
}
