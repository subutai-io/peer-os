package io.subutai.core.executor.impl;


import java.security.PrivilegedAction;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.security.auth.Subject;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.CommandResultImpl;
import io.subutai.common.command.CommandStatus;
import io.subutai.common.command.Request;
import io.subutai.common.command.Response;
import io.subutai.common.command.ResponseType;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.identity.api.model.Session;
import io.subutai.core.security.api.SecurityManager;


/**
 * Represents a single command process, Encapsulates command results
 */
class CommandProcess
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandProcess.class.getName() );

    private CommandCallback callback;
    private StringBuilder stdOut;
    private StringBuilder stdErr;
    private Integer exitCode;
    private CommandProcessor commandProcessor;
    volatile CommandStatus status;
    Semaphore semaphore;
    protected ExecutorService executor;
    private Request request;
    Session userSession;
    private int expectedResponseNumber = 1;
    Set<Response> queuedResponses;
    private String rhId;
    private AtomicBoolean isSent = new AtomicBoolean( false );
    private String encryptedRequest;


    CommandProcess( final CommandProcessor commandProcessor, final CommandCallback callback, final Request request,
                    final String rhId, final Session userSession )
    {
        Preconditions.checkNotNull( commandProcessor );
        Preconditions.checkNotNull( callback );
        Preconditions.checkNotNull( request );
        Preconditions.checkArgument( !StringUtils.isBlank( rhId ) );

        this.rhId = rhId;

        this.commandProcessor = commandProcessor;
        this.callback = callback;

        stdOut = new StringBuilder();
        stdErr = new StringBuilder();
        status = CommandStatus.NEW;
        semaphore = new Semaphore( 0 );

        this.request = request;
        this.userSession = userSession;

        queuedResponses = Sets.newTreeSet( new Comparator<Response>()
        {
            @Override
            public int compare( final Response o1, final Response o2 )
            {
                return Integer.compare( o1.getResponseNumber(), o2.getResponseNumber() );
            }
        } );

        this.encryptedRequest = encrypt( request );
    }


    protected String encrypt( Request request )
    {
        SecurityManager securityManager = ServiceLocator.lookup( SecurityManager.class );

        try
        {
            return securityManager.signNEncryptRequestToHost( JsonUtil.toJsonMinified( request ), request.getId() );
        }
        catch ( PGPException e )
        {
            throw new ActionFailedException( String.format( "Failed to encrypt command: %s", e.getMessage() ) );
        }
    }


    CommandResult waitResult()
    {
        try
        {
            semaphore.acquire();
        }
        catch ( InterruptedException e )
        {
            Thread.currentThread().interrupt();
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


    synchronized void processResponse( final Response response )
    {

        if ( expectedResponseNumber == response.getResponseNumber() )
        {
            expectedResponseNumber++;

            processNextResponse( response );

            for ( Response queuedResponse : queuedResponses )
            {
                if ( expectedResponseNumber == queuedResponse.getResponseNumber() )
                {
                    expectedResponseNumber++;

                    processNextResponse( queuedResponse );
                }
            }
        }
        else
        {
            //queue this response to feed in the next round
            queuedResponses.add( response );
        }
    }


    void processNextResponse( final Response response )
    {
        final CommandProcess THIS = this;

        if ( userSession != null )
        {

            Subject.doAs( userSession.getSubject(), new PrivilegedAction<Void>()
            {
                @Override
                public Void run()
                {
                    try
                    {
                        executor.execute( new ResponseProcessor( response, THIS, commandProcessor, request ) );
                    }
                    catch ( Exception e )
                    {
                        LOG.error( "Error in processResponse", e );
                    }
                    return null;
                }
            } );
        }
        else
        {
            executor.execute( new ResponseProcessor( response, THIS, commandProcessor, request ) );
        }
    }


    void start() throws CommandException
    {
        if ( status != CommandStatus.NEW )
        {
            throw new CommandException( "Command is already started" );
        }

        status = CommandStatus.RUNNING;
        executor = Executors.newSingleThreadExecutor();
    }


    boolean isDone()
    {
        return !( status == CommandStatus.RUNNING || status == CommandStatus.NEW );
    }


    void appendResponse( Response response )
    {
        if ( response != null )
        {
            if ( !StringUtils.isBlank( response.getStdOut() ) )
            {
                stdOut.append( response.getStdOut() );
            }
            if ( !StringUtils.isBlank( response.getStdErr() ) )
            {
                stdErr.append( response.getStdErr() );
            }

            exitCode = response.getExitCode();


            if ( response.getType() == ResponseType.EXECUTE_TIMEOUT )
            {
                status = CommandStatus.KILLED;
            }
            else if ( exitCode != null )
            {
                status = exitCode == 0 ? CommandStatus.SUCCEEDED : CommandStatus.FAILED;
            }
        }
    }


    protected CommandCallback getCallback()
    {
        return callback;
    }


    CommandResult getResult()
    {
        return new CommandResultImpl( exitCode, stdOut.toString(), stdErr.toString(), status );
    }


    boolean markAsSent()
    {
        return isSent.compareAndSet( false, true );
    }


    boolean isSent()
    {
        return isSent.get();
    }


    String getRhId()
    {
        return rhId;
    }


    String getEncryptedRequest()
    {
        return encryptedRequest;
    }
}
