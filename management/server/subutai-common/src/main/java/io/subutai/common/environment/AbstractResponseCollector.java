package io.subutai.common.environment;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.subutai.common.task.ResponseCollector;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;
import io.subutai.common.tracker.OperationMessage;


/**
 * Abstract group response
 */
public abstract class AbstractResponseCollector<R extends TaskRequest, T extends TaskResponse>
        implements ResponseCollector<R, T>
{
    private final String peerId;
    private List<T> responses = new ArrayList<>();
    transient private List<Future<Task>> tasks = new ArrayList<>();
    protected AtomicInteger counter = new AtomicInteger( 0 );
    protected boolean succeeded = true;
    private List<OperationMessage> messages = new CopyOnWriteArrayList<>();


    public AbstractResponseCollector( final String peerId )
    {
        this.peerId = peerId;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    abstract public void onSuccess( R request, final T response );


    @Override
    abstract public void onFailure( R request, final List<Throwable> exceptions );


    public void addResponse( T response, final String message )
    {
        if ( response == null )
        {
            throw new IllegalArgumentException( "Task response could not be null." );
        }
        this.responses.add( response );
        counter.incrementAndGet();
        addMessage( OperationMessage.Type.SUCCEEDED, message );
    }


    public void addFailure( final String message, final List<Throwable> exceptions )
    {
        counter.incrementAndGet();
        StringBuilder sb = new StringBuilder();
        for ( Throwable throwable : exceptions )
        {
            sb.append( throwable.getMessage() );
        }
        addMessage( OperationMessage.Type.FAILED, message, sb.toString() );
        succeeded = false;
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
