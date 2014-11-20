package org.safehaus.subutai.core.peer.impl.command;


import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;


public class BlockingCommandCallback implements CommandCallback
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
        try
        {
            completionSemaphore.acquire();
        }
        catch ( InterruptedException e )
        {
            //ignore
        }
        return commandResult;
    }
}
