package org.safehaus.subutai.core.command.api.command;


import org.safehaus.subutai.common.cache.EntryExpiryCallback;
import org.safehaus.subutai.common.command.CommandStatus;


/**
 * Callback for command expiry event
 */
public class CommandExecutorExpiryCallback implements EntryExpiryCallback<CommandExecutor>
{

    @Override
    public void onEntryExpiry( final CommandExecutor entry )
    {

        //obtain command lock
        entry.getCommand().getUpdateLock();
        try
        {
            //set command status to TIMEOUT if it is not completed or interrupted yet
            if ( !( entry.getCommand().hasCompleted() || entry.getCallback().isStopped() ) )
            {
                entry.getCommand().setCommandStatus( CommandStatus.TIMEOUT );
            }
            //call this to notify all waiting threads that command timed out
            entry.getCommand().notifyWaitingThreads();
            //shutdown command executor
            entry.getExecutor().shutdown();
        }
        finally
        {
            entry.getCommand().releaseUpdateLock();
        }
    }
}
