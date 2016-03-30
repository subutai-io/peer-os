package io.subutai.core.localpeer.impl.container;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
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
        return new RequestBuilder( getCommandBatch().toString() ).withTimeout( getTimeout() );
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
            buildResponse();
            success();
        }
        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
        this.finished = System.currentTimeMillis();
    }


    protected void buildResponse() throws Exception
    {
        if ( getResponseBuilder() == null )
        {
            throw new CommandException( "Response builder not found." );
        }
        response = getResponseBuilder().build( request, commandResult, getElapsedTime() );
    }


    protected void success() throws Exception
    {

        if ( onSuccessHandler != null )
        {
            try
            {
                onSuccessHandler.handle( this, request, response );
            }
            catch ( Exception e )
            {
                LOG.warn( "Exception on executing success handler.", e );
                exceptions.add( e );
                throw e;
            }
        }

        this.state = State.SUCCESS;
    }


    protected void failure( String message, Exception exception )
    {
        LOG.error( message, exception );
        if ( onFailureHandler != null )
        {
            try
            {
                onFailureHandler.handle( this, request, response );
            }
            catch ( Exception e )
            {
                LOG.warn( "Exception on executing failure handler.", e );
                exceptions.add( e );
            }
        }
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
    public int getNumberOfParallelTasks()
    {
        //by default no limit for parallel tasks
        return -1;
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


    @Override
    public long getFinished()
    {
        return finished;
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


    @Override
    public CommandStatus getCommandStatus()
    {
        return commandResult != null ? commandResult.getStatus() : null;
    }


    @Override
    public Integer getExitCode()
    {
        return commandResult != null ? commandResult.getExitCode() : null;
    }


    @Override
    public String getStdOut()
    {
        return ( commandResult != null && commandResult.getStdOut() != null ? commandResult.getStdOut() : "" );
    }


    @Override
    public String getStdErr()
    {
        return ( commandResult != null && commandResult.getStdErr() != null ? commandResult.getStdErr() : "" );
    }
}
