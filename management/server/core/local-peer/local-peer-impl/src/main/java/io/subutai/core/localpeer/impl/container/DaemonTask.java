package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;


/**
 * Abstract task implementation
 */
public abstract class DaemonTask<T> extends AbstractTask<T> implements CommandResultParser<T>
{
    private long endTime;


    @Override
    protected RequestBuilder getRequestBuilder() throws Exception
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
            while ( result == null )
            {
                try
                {
                    TimeUnit.SECONDS.sleep( 1 );
                }
                catch ( InterruptedException e )
                {
                    // ignore
                }
                result = lookupResult();
                if ( result == null )
                {
                    checkTimeout();
                }
            }
        }

        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
        return result;
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
            if ( endTime < System.currentTimeMillis() )
            {
                throw new CommandException( "Command execution timed out." );
            }
        }
    }
}
