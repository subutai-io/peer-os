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
import io.subutai.common.command.CommandUtil;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.peer.Host;
import io.subutai.common.task.Task;


/**
 * Abstract task implementation
 */
public abstract class AbstractTask<T> implements Task<T>
{
    public static int DEFAULT_TIMEOUT = 30;

    protected static final Logger LOG = LoggerFactory.getLogger( AbstractTask.class );

    protected T result;
    protected List<Throwable> exceptions = new ArrayList<>();
    private volatile Task.State state = Task.State.PENDING;
    protected CommandUtil commandUtil = new CommandUtil();
    protected CommandResult commandResult;
    protected long started;


    protected RequestBuilder getRequestBuilder() throws Exception
    {
        return new RequestBuilder( isChain() ? getCommandBatch().asChain() :
                                   String.format( "subutai batch -json '%s'", getCommandBatch().asJson() ) )
                .withTimeout( getTimeout() );
    }


    @Override
    public void start()
    {
        this.started = System.currentTimeMillis();
        this.state = State.RUNNING;
        try
        {
            RequestBuilder builder = getRequestBuilder();

            commandResult = commandUtil.execute( builder, getHost() );


            switch ( commandResult.getStatus() )
            {
                case SUCCEEDED:
                    success();
                    break;

                case FAILED:
                    throw new CommandException( commandResult.getStdErr() );

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
    }


    protected void success()
    {
        try
        {
            parseCommandResult();
            onSuccess();
            this.state = State.SUCCESS;
        }
        catch ( Exception e )
        {
            addException( e );
            this.state = State.FAILURE;
        }
    }


    protected void addException( final Throwable e )
    {
        this.exceptions.add( e );
    }


    protected void failure( String message, Throwable throwable )
    {

        addException( throwable );
        try
        {
            onFailure();
        }
        catch ( Exception e )
        {
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


    private void parseCommandResult() throws CommandResultParseException
    {
        if ( result == null && getCommandResultParser() != null )
        {
            try
            {
                result = getCommandResultParser().parse( commandResult );
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


    abstract public Host getHost();


    abstract public CommandResultParser<T> getCommandResultParser();


    @Override
    public boolean isSequential()
    {
        return false;
    }


    protected void onSuccess()
    {
        //empty
    }


    protected void onFailure()
    {
        // empty
    }


    public boolean isChain()
    {
        return true;
    }


    @Override
    public T getResult()
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
        return result;
    }


    @Override
    public boolean isDone() {return this.state == State.SUCCESS || this.state == State.FAILURE;}
}
