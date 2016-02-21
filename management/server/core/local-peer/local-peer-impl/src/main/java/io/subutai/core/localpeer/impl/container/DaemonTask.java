package io.subutai.core.localpeer.impl.container;


import io.subutai.common.command.RequestBuilder;


/**
 * Abstract task implementation
 */
public abstract class DaemonTask<T> extends AbstractTask<T>
{
    @Override
    protected RequestBuilder getRequestBuilder() throws Exception
    {
        return new RequestBuilder( String.format( "subutai batch -json '%s'", getCommandBatch().asJson() ) )
                .withTimeout( 1 ).daemon();
    }


    @Override
    protected void checkTimeout()
    {
        super.checkTimeout();
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
