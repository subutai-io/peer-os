package io.subutai.core.executor.impl;


import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseType;
import io.subutai.core.identity.api.model.User;


/**
 * Represents a single command process, Encapsulates command results
 */
public class CommandProcess
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcess.class.getName() );

    private CommandCallback callback;
    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private Integer exitCode;
    private CommandProcessor commandProcessor;
    protected volatile CommandStatus status;
    protected Semaphore semaphore;
    protected ExecutorService executor;
    private User user;


    public CommandProcess( final CommandProcessor commandProcessor, final CommandCallback callback, final User user )
    {
        Preconditions.checkNotNull( commandProcessor );
        Preconditions.checkNotNull( callback );

        this.commandProcessor = commandProcessor;
        this.callback = callback;

        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        status = CommandStatus.NEW;
        semaphore = new Semaphore( 0 );

        this.user = user;
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
        if ( status == CommandStatus.RUNNING )
        {
            status = CommandStatus.TIMEOUT;
        }

        semaphore.release();

        executor.shutdown();
    }


    public void processResponse( final Response response )
    {
        final CommandProcess THIS = this;
        Subject.doAs( user.getSubject(), new PrivilegedAction<Void>()
        {
            @Override
            public Void run()
            {
                try
                {
                    executor.execute( new ResponseProcessor( response, THIS, commandProcessor ) );
                }
                catch ( Exception e )
                {
                    LOG.error( "Error in processResponse", e );
                }
                return null;
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


    protected boolean isDone()
    {
        return !( status == CommandStatus.RUNNING || status == CommandStatus.NEW );
    }


    protected void appendResponse( Response response )
    {
        if ( response != null )
        {
            if ( !Strings.isNullOrEmpty( response.getStdOut() ) )
            {
                stdOut.append( response.getStdOut() );
            }
            if ( !Strings.isNullOrEmpty( response.getStdErr() ) )
            {
                stdErr.append( response.getStdErr() );
            }

            exitCode = response.getExitCode();

            if ( exitCode != null )
            {
                status = exitCode == 0 ? CommandStatus.SUCCEEDED : CommandStatus.FAILED;
            }
            else if ( response.getType() == ResponseType.EXECUTE_TIMEOUT )
            {
                status = CommandStatus.KILLED;
            }
            else if ( response.getType() == ResponseType.LIST_INOTIFY_RESPONSE
                    || response.getType() == ResponseType.PS_RESPONSE
                    || response.getType() == ResponseType.SET_INOTIFY_RESPONSE
                    || response.getType() == ResponseType.UNSET_INOTIFY_RESPONSE )
            {
                status = CommandStatus.SUCCEEDED;
            }
        }
    }


    protected CommandCallback getCallback()
    {
        return callback;
    }


    protected CommandResult getResult()
    {
        return new CommandResultImpl( exitCode, stdOut.toString(), stdErr.toString(), status );
    }
}
