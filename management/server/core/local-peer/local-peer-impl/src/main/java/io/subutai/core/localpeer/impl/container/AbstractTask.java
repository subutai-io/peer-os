package io.subutai.core.localpeer.impl.container;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskCallbackHandler;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;
import io.subutai.common.task.TaskResponseBuilder;


/**
 * Abstract task implementation
 */
public abstract class AbstractTask<R extends TaskRequest, T extends TaskResponse> implements Task<R, T>
{
    public static int DEFAULT_TIMEOUT = 30;

    protected static final Logger LOG = LoggerFactory.getLogger( AbstractTask.class );
    private int id;
    protected R request;
    protected T response;
    protected List<Throwable> exceptions = new ArrayList<>();
    private volatile Task.State state = Task.State.PENDING;
    protected CommandResult commandResult;
    protected long started;
    protected long finished;
    protected TaskCallbackHandler<R, T> onSuccessHandler;
    protected TaskCallbackHandler<R, T> onFailureHandler;


    public AbstractTask( final R request )
    {
        this.request = request;
    }


    @Override
    public RequestBuilder getRequestBuilder() throws Exception
    {
        return new RequestBuilder( isChain() ? getCommandBatch().asChain() :
                                   String.format( "subutai batch -json '%s'", getCommandBatch().asJson() ) )
                .withTimeout( getTimeout() );
    }


    public int getId()
    {
        return id;
    }


    @Override
    public void start( final int id )
    {
        this.started = System.currentTimeMillis();
        this.id = id;
        this.state = State.RUNNING;
    }


    @Override
    public void done( final CommandResult commandResult )
    {
        this.commandResult = commandResult;
        try
        {
            processCommandResult();
            success();
        }
        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
        this.finished = System.currentTimeMillis();
    }


    protected void success()
    {
        this.state = State.SUCCESS;
    }


    protected void failure( String message, Exception exeption )
    {
        LOG.error( message, exeption );
        this.state = State.FAILURE;
    }


    @Override
    public State getState()
    {
        return state;
    }


    @Override
    public List<Throwable> getExceptions()
    {
        return exceptions;
    }


    @Override
    public String getExceptionsAsString()
    {
        StringBuilder b = new StringBuilder();
        for ( Throwable throwable : exceptions )
        {
            b.append( String.format( "%s\n", throwable.getMessage() ) );
        }
        return b.toString();
    }


    @Override
    public int getTimeout()
    {
        return DEFAULT_TIMEOUT;
    }


    @Override
    public boolean isSequential()
    {
        return false;
    }


    public boolean isChain()
    {
        return true;
    }


    @Override
    public R getRequest()
    {
        return request;
    }


    @Override
    public T waitAndGetResponse()
    {
        while ( !isDone() )
        {
            try
            {
                TimeUnit.MICROSECONDS.sleep( 500 );
            }
            catch ( InterruptedException e )
            {
                // ignore
            }
        }

        return response;
    }


    private void processCommandResult() throws Exception
    {
        response = getResponseBuilder().build( request, commandResult, getElapsedTime() );

        if ( commandResult.hasSucceeded() )
        {
            if ( onSuccessHandler != null )
            {
                onSuccessHandler.handle( this, request, response );
            }
        }
        else
        {

            if ( onFailureHandler != null )
            {
                onFailureHandler.handle( this, request, response );
            }
        }
    }


    @Override
    public boolean isDone() {return this.state == State.SUCCESS || this.state == State.FAILURE;}


    @Override
    public long getElapsedTime()
    {
        if ( isDone() )
        {
            return this.finished - this.started;
        }
        else
        {
            return System.currentTimeMillis() - this.started;
        }
    }


    public void onSuccess( TaskCallbackHandler<R, T> handler )
    {
        this.onSuccessHandler = handler;
    }


    public void onFailure( TaskCallbackHandler<R, T> handler )
    {
        this.onFailureHandler = handler;
    }


    public boolean isSucceeded()
    {
        return this.state == State.SUCCESS;
    }


    abstract public TaskResponseBuilder<R, T> getResponseBuilder();


    public String getStdOut()
    {
        return ( commandResult != null && commandResult.getStdOut() != null ? commandResult.getStdOut() : "" );
    }


    public String getStdErr()
    {
        return ( commandResult != null && commandResult.getStdErr() != null ? commandResult.getStdErr() : "" );
    }
}
