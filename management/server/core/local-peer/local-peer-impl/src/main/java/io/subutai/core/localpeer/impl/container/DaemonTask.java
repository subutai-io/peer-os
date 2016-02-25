package io.subutai.core.localpeer.impl.container;


import java.util.concurrent.TimeUnit;

import io.subutai.common.command.CommandException;
import io.subutai.common.command.RequestBuilder;


/**
 * Abstract task implementation
 */
public abstract class DaemonTask<T> extends AbstractTask<T>
{
    private boolean run = true;


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
        this.started = System.currentTimeMillis();
        setState( State.RUNNING );
        try
        {
            RequestBuilder builder = getRequestBuilder();

            commandResult = commandUtil.execute( builder, getHost() );

            if ( !commandResult.hasSucceeded() )
            {
                setState( State.FAILURE );
            }
        }

        catch ( Exception e )
        {
            this.exception = e;
            setState( State.FAILURE );
        }

        while ( run && getState() == State.RUNNING )
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


    @Override
    public boolean isSequential()
    {
        return true;
    }


    public void stop()
    {
        this.run = false;
    }


    private void checkTimeout()
    {
        if ( getState() == State.RUNNING )
        {
            if ( started + TimeUnit.SECONDS.toMillis( getTimeout() ) < System.currentTimeMillis() )
            {
                this.exception = new CommandException( "Command execution timeout." );
                setState( State.FAILURE );
            }
        }
        if ( result == null )
        {
            if ( getCommandResultParser() != null )
            {
                try
                {
                    result = getCommandResultParser().parse( commandResult );
                    if ( result != null )
                    {
                        setState( State.SUCCESS );
                    }
                }
                catch ( Exception e )
                {
                    // ignore
                }
            }
        }
    }
}
