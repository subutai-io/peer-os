package org.safehaus.subutai.core.command.api.command;


import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Provides common Command functionality
 */
public abstract class AbstractCommand implements Command
{
    private static final Logger LOG = LoggerFactory.getLogger( AbstractCommand.class.getName() );
    //subset of requests to send to agents
    protected final Set<Request> requests = new HashSet<>();
    //lock used to synchronize update of command state between command executor thread and cache evictor thread
    private final Lock updateLock = new ReentrantLock( true );
    //holds map of results of command execution where key is agent's UUID and value is AgentResult
    private final Map<UUID, AgentResult> results = new HashMap<>();
    //semaphore used to wait until command completes or times out
    private final Semaphore completionSemaphore = new Semaphore( 0 );
    private final CommandRunnerBase commandRunner;
    //number of requests sent to agents
    protected int requestsCount;
    //uuid of command
    protected UUID commandUUID;
    //command timeout
    protected int timeout;
    //command description
    protected String description;
    //status of command
    protected volatile CommandStatus commandStatus = CommandStatus.NEW;
    //indicates if this command is broadcast command
    protected boolean broadcastCommand;
    //number of requests completed so far
    private AtomicInteger requestsCompleted = new AtomicInteger();
    //number of requests succeeded so far
    private AtomicInteger requestsSucceeded = new AtomicInteger();
    //custom object assigned to this command
    private Object data;


    protected AbstractCommand( final CommandRunnerBase commandRunner )
    {
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        this.commandRunner = commandRunner;
    }


    /**
     * Shows if command has completed. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED ||
     * command.getCommandStatus == CommandStatus.FAILED
     *
     * @return - true if completed, false otherwise
     */
    @Override
    public boolean hasCompleted()
    {
        return commandStatus == CommandStatus.FAILED || commandStatus == CommandStatus.SUCCEEDED;
    }


    /**
     * Shows if command has succeeded. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED
     *
     * @return - true if succeeded, false otherwise
     */
    @Override
    public boolean hasSucceeded()
    {
        return commandStatus == CommandStatus.SUCCEEDED;
    }


    /**
     * Returns command status
     *
     * @return - status of command
     */
    @Override
    public CommandStatus getCommandStatus()
    {
        return commandStatus;
    }


    /**
     * Sets command status
     *
     * @param commandStatus - new status of command
     */
    public void setCommandStatus( CommandStatus commandStatus )
    {
        this.commandStatus = commandStatus;
    }


    /**
     * Returns map of results from agents where key is agent's UUID and value is instance of AgentResult
     *
     * @return - map of agents' results
     */
    @Override
    public Map<UUID, AgentResult> getResults()
    {
        return Collections.unmodifiableMap( results );
    }


    /**
     * Returns command UUID
     *
     * @return - UUID of command
     */
    @Override
    public UUID getCommandUUID()
    {
        return commandUUID;
    }


    /**
     * Returns custom object assigned to this command or null
     *
     * @return - custom object or null
     */
    @Override
    public Object getData()
    {
        return data;
    }


    /**
     * Assigns custom object to this command
     *
     * @param data - custom object
     */
    @Override
    public void setData( Object data )
    {
        this.data = data;
    }


    /**
     * Returns all std err outputs from agents joined in one string
     *
     * @return - all std err outputs from agents joined in one string
     */
    @Override
    public String getAllErrors()
    {
        StringBuilder errors = new StringBuilder();
        for ( Map.Entry<UUID, AgentResult> result : results.entrySet() )
        {
            AgentResult agentResult = result.getValue();
            if ( !Strings.isNullOrEmpty( agentResult.getStdErr() ) || agentResult.getExitCode() != null )
            {
                errors.append( agentResult.getAgentUUID() ).
                        append( ": " ).
                              append( agentResult.getStdErr() ).
                              append( "; Exit code: " ).
                              append( agentResult.getExitCode() ).
                              append( "\n" );
            }
        }
        return errors.toString();
    }


    /**
     * Returns command description or null
     *
     * @return - description of command
     */
    @Override
    public String getDescription()
    {
        return description;
    }


    @Override
    public void execute( final CommandCallback callback ) throws CommandException
    {
        executeCommand( callback, false );
    }


    @Override
    public void executeAsync( final CommandCallback callback ) throws CommandException
    {
        executeCommand( callback, true );
    }


    @Override
    public void execute() throws CommandException
    {
        execute( null );
    }


    @Override
    public void executeAsync() throws CommandException
    {
        executeAsync( null );
    }


    public void executeCommand( final CommandCallback callback, boolean async ) throws CommandException
    {
        if ( this.commandStatus != CommandStatus.NEW )
        {
            throw new CommandException( String.format( "Command status must be %s", CommandStatus.NEW.name() ) );
        }

        try
        {
            if ( async )
            {
                if ( callback == null )
                {
                    commandRunner.runCommandAsync( this );
                }
                else
                {
                    commandRunner.runCommandAsync( this, callback );
                }
            }
            else
            {
                if ( callback == null )
                {
                    commandRunner.runCommand( this );
                }
                else
                {
                    commandRunner.runCommand( this, callback );
                }
            }
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Error in executeCommand", e );
            throw new CommandException( e.getMessage() );
        }
    }


    /**
     * Updates relevant {@code AgentResult} for agent associated with this response
     */
    public void appendResult( Response response )
    {
        if ( response != null && response.getUuid() != null )
        {

            AgentResultImpl agentResult = ( AgentResultImpl ) results.get( response.getUuid() );
            if ( agentResult == null )
            {
                agentResult = new AgentResultImpl( response.getUuid() );
                results.put( agentResult.getAgentUUID(), agentResult );
            }

            agentResult.appendResults( response );

            if ( response.isFinal() )
            {
                incrementCompletedRequestsCount();
                if ( response.hasSucceeded() )
                {
                    incrementSucceededRequestsCount();
                }
                if ( getRequestsCompleted() == getRequestsCount() )
                {
                    setCommandStatus( getRequestsCompleted() == getRequestsSucceeded() ? CommandStatus.SUCCEEDED :
                                      CommandStatus.FAILED );
                }
            }
        }
    }


    /**
     * Increments count of completed requests
     */
    public void incrementCompletedRequestsCount()
    {
        requestsCompleted.incrementAndGet();
    }


    /**
     * Increments count of succeeded requests
     */
    public void incrementSucceededRequestsCount()
    {
        requestsSucceeded.incrementAndGet();
    }


    /**
     * Returns number of requests completed so far
     *
     * @return - number of completed requests
     */
    public int getRequestsCompleted()
    {
        return requestsCompleted.get();
    }


    /**
     * Returns count of requests in this command
     *
     * @return number of requests in command
     */
    public int getRequestsCount()
    {
        return requestsCount;
    }


    /**
     * Returns number of requests succeeded so far
     *
     * @return - number of succeeded requests
     */
    public int getRequestsSucceeded()
    {
        return requestsSucceeded.get();
    }


    /**
     * Blocks caller until command has completed or timed out
     */
    public void waitCompletion()
    {
        try
        {
            completionSemaphore.acquire();
        }
        catch ( InterruptedException e )
        {
            LOG.warn( "Interrupted in waitCompletion", e );
        }
    }


    /**
     * Notifies waiting threads which called waitCompletion() that command has completed or timed out
     */
    public void notifyWaitingThreads()
    {
        completionSemaphore.release();
    }


    /**
     * Acquires update lock of this command
     */
    public void getUpdateLock()
    {
        updateLock.lock();
    }


    /**
     * Releases update lock of this command
     */
    public void releaseUpdateLock()
    {
        updateLock.unlock();
    }


    /**
     * Return timeout of command, which is the maximum timeout among all requests of this command
     *
     * @return - command timeout
     */
    public int getTimeout()
    {
        return timeout;
    }


    public Set<Request> getRequests()
    {
        return Collections.unmodifiableSet( requests );
    }


    /**
     * Indicates if this command is broadcast command or not
     *
     * @return true - broadcast command, false - not broadcast command
     */
    public boolean isBroadcastCommand()
    {
        return broadcastCommand;
    }
}

