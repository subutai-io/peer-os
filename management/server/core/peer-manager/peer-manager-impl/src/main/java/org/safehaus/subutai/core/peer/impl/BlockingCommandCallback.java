package org.safehaus.subutai.core.peer.impl;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.Response;


public class BlockingCommandCallback extends CommandCallback
{
    private final Semaphore completionSemaphore;
    private final CommandCallback callback;
    private CommandResult commandResult;


    public BlockingCommandCallback( CommandCallback callback )
    {
        this.callback = callback;
        this.completionSemaphore = new Semaphore( 0 );
    }


    @Override
    public void onResponse( final Response response, final CommandResult commandResult )
    {
        if ( callback != null )
        {
            callback.onResponse( response, commandResult );
        }
        this.commandResult = commandResult;
        if ( commandResult.hasCompleted() || commandResult.hasTimedOut() )
        {
            completionSemaphore.release();
        }
    }


    public Semaphore getCompletionSemaphore()
    {
        return completionSemaphore;
    }


    public CommandResult getCommandResult()
    {
        return commandResult;
    }


    public void waitCompletion()
    {
        try
        {
            completionSemaphore.acquire();
        }
        catch ( InterruptedException e )
        {
            //ignore
        }
    }
}
