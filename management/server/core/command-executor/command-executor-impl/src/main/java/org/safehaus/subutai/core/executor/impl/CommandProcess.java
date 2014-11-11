package org.safehaus.subutai.core.executor.impl;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.command.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Holds command data while command is in process
 */
public class CommandProcess
{
    protected static final Logger LOG = LoggerFactory.getLogger( CommandProcess.class.getName() );

    private CommandCallback callback;
    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private Integer exitCode;
    private volatile CommandStatus status;
    private Semaphore semaphore;
    private ExecutorService executor;
    private CommandProcessor commandProcessor;


    public CommandProcess( final CommandProcessor commandProcessor, final CommandCallback callback )
    {
        Preconditions.checkNotNull( commandProcessor, "Command processor is null" );
        Preconditions.checkNotNull( callback, "Callback is null" );

        this.commandProcessor = commandProcessor;
        this.callback = callback;

        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        status = CommandStatus.NEW;
        semaphore = new Semaphore( 0 );
    }


    public CommandResult waitResult()
    {
        try
        {
            semaphore.acquire();
        }
        catch ( InterruptedException e )
        {
            LOG.error( "ignore", e );
        }

        return getResult();
    }


    public void stop()
    {
        if ( !isCompleted() )
        {
            status = CommandStatus.TIMEOUT;
        }

        semaphore.release();

        executor.shutdown();
    }


    public void processResponse( final Response response )
    {
        executor.execute( new Runnable()
        {
            @Override
            public void run()
            {
                appendResponse( response );

                try
                {
                    callback.onResponse( response, getResult() );

                    if ( isCompleted() )
                    {
                        //remove process from command processor
                        commandProcessor.remove( response.getCommandId() );
                        //stop process
                        stop();
                    }
                }
                catch ( Exception e )
                {
                    LOG.error( "Error notifying callback", e );
                }
            }
        } );
    }


    public void start() throws CommandException
    {
        if ( status != CommandStatus.NEW )
        {
            throw new CommandException( "Command is already started" );
        }

        status = CommandStatus.RUNNING;

        executor = Executors.newSingleThreadExecutor();
    }


    private boolean isCompleted()
    {
        return callback.isStopped() || getResult().hasCompleted();
    }


    private void appendResponse( Response response )
    {
        if ( response != null )
        {
            if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
            {
                stdOut.append( response.getStdOut() );
            }
            if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
            {
                stdErr.append( response.getStdOut() );
            }

            exitCode = response.getExitCode();

            if ( exitCode != null )
            {
                status = exitCode == 0 ? CommandStatus.SUCCEEDED : CommandStatus.FAILED;
            }
            else if ( response.getType() == ResponseType.EXECUTE_TIMEOUT )
            {
                status = CommandStatus.FAILED;
            }
            else if ( response.getType() == ResponseType.LIST_INOTIFY_RESPONSE
                    || response.getType() == ResponseType.PS_RESPONSE )
            {
                status = CommandStatus.SUCCEEDED;
            }
        }
    }


    private CommandResult getResult()
    {
        return new CommandResultImpl( exitCode, stdOut.toString(), stdErr.toString(), status );
    }
}
