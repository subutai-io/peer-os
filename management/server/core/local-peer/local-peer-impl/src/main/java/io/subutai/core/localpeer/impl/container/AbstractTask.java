package io.subutai.core.localpeer.impl.container;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParseException;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.task.Task;
import io.subutai.common.task.TaskCallbackHandler;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;


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
            switch ( this.commandResult.getStatus() )
            {
                case SUCCEEDED:
                    success();
                    break;

                case FAILED:
                    throw new CommandException( this.commandResult.getStdErr() );

                case KILLED:
                    throw new CommandException( "Command killed." );

                case TIMEOUT:
                    throw new CommandException( "Command execution timed out." );

                default:
                    throw new CommandException( "Unexpected error on executing command." );
            }
        }
        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
        this.finished = System.currentTimeMillis();
    }


    protected void success()
    {
        try
        {
            parseCommandResult();
            if ( onSuccessHandler != null )
            {
                onSuccessHandler.handle( this, request, response );
            }
            this.state = State.SUCCESS;
        }
        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
    }


    protected void addException( final Throwable e )
    {
        this.exceptions.add( e );
    }


    protected void failure( String message, Throwable throwable )
    {
        LOG.error( throwable.getMessage(), throwable );

        addException( throwable );
        try
        {
            if ( onFailureHandler != null )
            {
                onFailureHandler.handle( this, request, response );
            }
        }
        catch ( Exception e )
        {
            LOG.error( throwable.getMessage(), throwable );
            addException( e );
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


    private void parseCommandResult() throws CommandResultParseException
    {
        if ( response == null && getCommandResultParser() != null )
        {
            try
            {
                response = getCommandResultParser().parse( commandResult );
            }
            catch ( Exception e )
            {
                throw new CommandResultParseException( e );
            }
        }
    }


    @Override
    public int getTimeout()
    {
        return DEFAULT_TIMEOUT;
    }


    abstract public CommandResultParser<T> getCommandResultParser();


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
    public T getResponse()
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


    public void onSuccess( TaskCallbackHandler<R, T> handler )
    {
        this.onSuccessHandler = handler;
    }


    public void onFailure( TaskCallbackHandler<R, T> handler )
    {
        this.onFailureHandler = handler;
    }
}
