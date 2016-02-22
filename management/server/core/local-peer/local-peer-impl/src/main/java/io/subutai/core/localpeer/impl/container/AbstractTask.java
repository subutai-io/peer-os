package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
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
    protected Exception exception;
    private Task.State state = Task.State.PENDING;
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

            setState( commandResult.hasSucceeded() ? State.SUCCESS : State.FAILURE );
        }

        catch ( Exception e )
        {
            this.exception = e;
            setState( State.FAILURE );
        }
    }


    @Override
    public State getState()
    {
        return state;
    }


    @Override
    public Exception getException()
    {
        return exception;
    }


    private void parseCommandResult()
    {
        try
        {
            if ( result == null && getCommandResultParser() != null )
            {
                result = getCommandResultParser().parse( commandResult );
            }
        }
        catch ( Exception e )
        {
            exception = new CommandException( "Command result parse exception: " + e.getMessage() );
            setState( State.FAILURE );
        }

    }


    @Override
    public T getResult()
    {
        return result;
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

//    public void checkTimeout()
//    {
//        if ( this.state == State.RUNNING )
//        {
//            if ( started + TimeUnit.SECONDS.toMillis( getTimeout() ) < System.currentTimeMillis() )
//            {
//                this.exception = new CommandException( "Command execution timeout." );
//                setState( State.FAILURE );
//            }
//        }
//    }


    protected void onSuccess()
    {
        //empty
    }


    protected void onFailure()
    {
        // empty
    }


    protected void setState( final State state )
    {
        this.state = state;
        switch ( state )
        {
            case SUCCESS:
                try
                {
                    parseCommandResult();
                    onSuccess();
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage(), e );
                }
                break;
            case FAILURE:
                try
                {
                    parseCommandResult();
                    onFailure();
                }
                catch ( Exception e )
                {
                    LOG.error( e.getMessage(), e );
                }
        }
    }


    public boolean isChain()
    {
        return true;
    }
}
