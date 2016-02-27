package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.task.TaskRequest;
import io.subutai.common.task.TaskResponse;


/**
 * Abstract task implementation
 */
public abstract class DaemonTask<R extends TaskRequest, T extends TaskResponse> extends AbstractTask<R, T> implements CommandResultParser<T>
{
    public DaemonTask( final R request )
    {
        super( request );
    }


    @Override
    public RequestBuilder getRequestBuilder() throws Exception
    {
        return new RequestBuilder( isChain() ? getCommandBatch().asChain() :
                                   String.format( "subutai batch -json '%s'", getCommandBatch().asJson() ) )
                .withTimeout( 1 ).daemon();
    }


    @Override
    public CommandResultParser<T> getCommandResultParser()
    {
        return this;
    }


    @Override
    public T parse( final CommandResult commandResult )
    {
        // waiting for result
        try
        {
            while ( response == null )
            {
                try
                {
                    TimeUnit.SECONDS.sleep( 1 );
                }
                catch ( InterruptedException e )
                {
                    // ignore
                }
                response = lookupResult();
                if ( response == null )
                {
                    checkTimeout();
                }
            }
        }

        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
        return response;
    }


    public abstract T lookupResult();

/*

    @Override
    public void start()
    {
        super.start();

        endTime = started + TimeUnit.SECONDS.toMillis( getTimeout() );

        // waiting the result
        try
        {
            while ( !isDone() )
            {
                try
                {
                    TimeUnit.SECONDS.sleep( 1 );
                }
                catch ( InterruptedException e )
                {
                    // ignore
                }

                checkTimeout();
            }
        }

        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
    }
*/


    @Override
    public boolean isSequential()
    {
        return true;
    }


    protected void checkTimeout() throws CommandException
    {
        if ( getState() == State.RUNNING )
        {
            if ( finished < System.currentTimeMillis() )
            {
                throw new CommandException( "Command execution timed out." );
            }
        }
    }
}
