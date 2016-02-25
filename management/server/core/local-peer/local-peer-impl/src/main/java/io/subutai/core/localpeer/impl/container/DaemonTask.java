package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResultParser;
import io.subutai.common.command.RequestBuilder;


/**
 * Abstract task implementation
 */
public abstract class DaemonTask<T> extends AbstractTask<T>
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

                checkState();
            }
        }

        catch ( Exception e )
        {
            failure( e.getMessage(), e );
        }
    }


    /**
     * This method should look up of running command. If the result is found it should returns this result, otherwise
     * returns null.
     */
    public abstract T lookupResult();


    @Override
    public boolean isSequential()
    {
        return true;
    }


    @Override
    public CommandResultParser<T> getCommandResultParser()
    {
        return null;
    }


    protected void checkState() throws CommandException
    {
        if ( getState() == State.RUNNING )
        {
            if ( endTime < System.currentTimeMillis() )
            {
                throw new CommandException( "Command execution timed out." );
            }
        }
        if ( result == null )
        {
            try
            {
                result = lookupResult();
                if ( result != null )
                {
                    success();
                }
            }
            catch ( Exception e )
            {
                throw new CommandException( "Error on looking up daemon process result." );
            }
        }
    }
}
