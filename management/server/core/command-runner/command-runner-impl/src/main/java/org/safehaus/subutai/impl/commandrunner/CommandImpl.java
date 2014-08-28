/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.impl.commandrunner;


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

import org.safehaus.subutai.api.commandrunner.AgentRequestBuilder;
import org.safehaus.subutai.api.commandrunner.AgentResult;
import org.safehaus.subutai.api.commandrunner.Command;
import org.safehaus.subutai.api.commandrunner.CommandStatus;
import org.safehaus.subutai.api.commandrunner.RequestBuilder;
import org.safehaus.subutai.common.UUIDUtil;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.Response;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This is implementation of Command interface
 */
public class CommandImpl implements Command {

    //holds map of results of command execution where key is agent's UUID and value is AgentResult
    private final Map<UUID, AgentResult> results = new HashMap<>();
    //number of requests sent to agents
    private final int requestsCount;
    //uuid of command
    private final UUID commandUUID;
    //set of requests to send to agents produced from supplied request builders
    private final Set<Request> requests = new HashSet<>();
    //command timeout
    private final int timeout;
    //semaphore used to wait until command completes or times out
    private final Semaphore completionSemaphore = new Semaphore( 0 );
    //lock used to synchronize update of command state between command executor thread and cache evictor thread
    private final Lock updateLock = new ReentrantLock( true );
    //command description
    private final String description;
    //status of command
    private volatile CommandStatus commandStatus = CommandStatus.NEW;
    //number of requests completed so far
    private AtomicInteger requestsCompleted = new AtomicInteger();
    //number of requests succeeded so far
    private AtomicInteger requestsSucceeded = new AtomicInteger();
    //custom object assigned to this command
    private Object data;
    //indicates if this command is broadcast command
    private boolean broadcastCommand;


    /**
     * Constructor which initializes request based on supplied request builder. The same request produced by request
     * builder will be sent to all connected agents. This is a broadcast command.
     *
     * @param requestBuilder - request builder used to produce request
     * @param requestsCount - number of request to send
     */
    public CommandImpl( RequestBuilder requestBuilder, int requestsCount ) {

        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( requestsCount > 0, "Request Count <= 0" );

        this.description = null;
        this.broadcastCommand = true;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = requestsCount;
        this.timeout = requestBuilder.getTimeout();

        requests.add( requestBuilder.build( commandUUID, commandUUID ) );
    }


    /**
     * Constructor which initializes request based on supplied request builder and set of agents. The same request
     * produced by request builder will be sent to supplied set of agents
     *
     * @param description - command description
     * @param requestBuilder - request builder used to produce request
     * @param agents - target agents
     */
    public CommandImpl( String description, RequestBuilder requestBuilder, Set<Agent> agents ) {

        Preconditions.checkNotNull( requestBuilder, "Request Builder is null" );
        Preconditions.checkArgument( agents != null && !agents.isEmpty(), "Agents are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = agents.size();
        this.timeout = requestBuilder.getTimeout();

        for ( Agent agent : agents ) {
            requests.add( requestBuilder.build( agent.getUuid(), commandUUID ) );
        }
    }


    /**
     * Constructor which initializes request based on supplied request builders. Each agent will receive own custom
     * request produced by corresponding AgentRequestBuilder
     *
     * @param description - command description
     * @param requestBuilders - request builder used to produce request
     */
    public CommandImpl( String description, Set<AgentRequestBuilder> requestBuilders ) {
        Preconditions.checkArgument( requestBuilders != null && !requestBuilders.isEmpty(),
                "Request Builders are null or empty" );

        this.description = description;
        this.commandUUID = UUIDUtil.generateTimeBasedUUID();
        this.requestsCount = requestBuilders.size();

        int maxTimeout = 0;
        for ( AgentRequestBuilder requestBuilder : requestBuilders ) {
            requests.add( requestBuilder.build( requestBuilder.getAgent().getUuid(), commandUUID ) );
            if ( requestBuilder.getTimeout() > maxTimeout ) {
                maxTimeout = requestBuilder.getTimeout();
            }
        }

        this.timeout = maxTimeout;
    }


    /**
     * Shows if command has completed. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED ||
     * command.getCommandStatus == CommandStatus.FAILED
     *
     * @return - true if completed, false otherwise
     */
    public boolean hasCompleted() {
        return commandStatus == CommandStatus.FAILED || commandStatus == CommandStatus.SUCCEEDED;
    }


    /**
     * Shows if command has succeeded. The same as checking command.getCommandStatus == CommandStatus.SUCCEEDED
     *
     * @return - true if succeeded, false otherwise
     */
    public boolean hasSucceeded() {
        return commandStatus == CommandStatus.SUCCEEDED;
    }


    /**
     * Returns command status
     *
     * @return - status of command
     */
    public CommandStatus getCommandStatus() {
        return commandStatus;
    }


    /**
     * Sets command status
     *
     * @param commandStatus - new status of command
     */
    public void setCommandStatus( CommandStatus commandStatus ) {
        this.commandStatus = commandStatus;
    }


    /**
     * Returns map of results from agents where key is agent's UUID and value is instance of AgentResult
     *
     * @return - map of agents' results
     */
    public Map<UUID, AgentResult> getResults() {
        return Collections.unmodifiableMap( results );
    }


    /**
     * Returns command UUID
     *
     * @return - UUID of command
     */
    public UUID getCommandUUID() {
        return commandUUID;
    }


    /**
     * Returns custom object assigned to this command or null
     *
     * @return - custom object or null
     */
    public Object getData() {
        return data;
    }


    /**
     * Assigns custom object to this command
     *
     * @param data - custom object
     */
    public void setData( Object data ) {
        this.data = data;
    }


    /**
     * Returns all std err outputs from agents joined in one string
     *
     * @return - all std err outputs from agents joined in one string
     */
    public String getAllErrors() {
        StringBuilder errors = new StringBuilder();
        for ( Map.Entry<UUID, AgentResult> result : results.entrySet() ) {
            AgentResult agentResult = result.getValue();
            if ( !Strings.isNullOrEmpty( agentResult.getStdErr() ) || agentResult.getExitCode() != null ) {
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
    public String getDescription() {
        return description;
    }


    /**
     * Updates relevant {@code AgentResult} for agent associated with this response
     */
    public void appendResult( Response response ) {
        if ( response != null && response.getUuid() != null ) {

            AgentResultImpl agentResult = ( AgentResultImpl ) results.get( response.getUuid() );
            if ( agentResult == null ) {
                agentResult = new AgentResultImpl( response.getUuid() );
                results.put( agentResult.getAgentUUID(), agentResult );
            }

            agentResult.appendResults( response );

            if ( response.isFinal() ) {
                incrementCompletedRequestsCount();
                if ( response.hasSucceeded() ) {
                    incrementSucceededRequestsCount();
                }
                if ( getRequestsCompleted() == getRequestsCount() ) {
                    if ( getRequestsCompleted() == getRequestsSucceeded() ) {
                        setCommandStatus( CommandStatus.SUCCEEDED );
                    }
                    else {
                        setCommandStatus( CommandStatus.FAILED );
                    }
                }
            }
        }
    }


    /**
     * Increments count of completed requests
     */
    public void incrementCompletedRequestsCount() {
        requestsCompleted.incrementAndGet();
    }


    /**
     * Increments count of succeeded requests
     */
    public void incrementSucceededRequestsCount() {
        requestsSucceeded.incrementAndGet();
    }


    /**
     * Returns number of requests completed so far
     *
     * @return - number of completed requests
     */
    public int getRequestsCompleted() {
        return requestsCompleted.get();
    }


    /**
     * Returns count of requests in this command
     *
     * @return number of requests in command
     */
    public int getRequestsCount() {
        return requestsCount;
    }


    /**
     * Returns number of requests succeeded so far
     *
     * @return - number of succeeded requests
     */
    public int getRequestsSucceeded() {
        return requestsSucceeded.get();
    }


    /**
     * Blocks caller until command has completed or timed out
     */
    public void waitCompletion() {
        try {
            completionSemaphore.acquire();
        }
        catch ( InterruptedException ex ) {
        }
    }


    /**
     * Notifies waiting threads which called waitCompletion() that command has completed or timed out
     */
    public void notifyWaitingThreads() {
        completionSemaphore.release();
    }


    /**
     * Acquires update lock of this command
     */
    public void getUpdateLock() {
        updateLock.lock();
    }


    /**
     * Releases update lock of this command
     */
    public void releaseUpdateLock() {
        updateLock.unlock();
    }


    /**
     * Return timeout of command, which is the maximum timeout among all requests of this command
     *
     * @return - command timeout
     */
    public int getTimeout() {
        return timeout;
    }


    /**
     * Returns all request of this command
     *
     * @return - requests of command
     */
    public Set<Request> getRequests() {
        return Collections.unmodifiableSet( requests );
    }


    /**
     * Indicates if this command is broadcast command or not
     *
     * @return true - broadcast command, false - not broadcast command
     */
    public boolean isBroadcastCommand() {
        return broadcastCommand;
    }


    @Override
    public String toString() {
        return "CommandImpl{" +
                "results=" + results +
                ", requestsCount=" + requestsCount +
                ", commandUUID=" + commandUUID +
                ", requests=" + requests +
                ", timeout=" + timeout +
                ", completionSemaphore=" + completionSemaphore +
                ", updateLock=" + updateLock +
                ", description='" + description + '\'' +
                ", commandStatus=" + commandStatus +
                ", requestsCompleted=" + requestsCompleted +
                ", requestsSucceeded=" + requestsSucceeded +
                ", data=" + data +
                ", broadcastCommand=" + broadcastCommand +
                '}';
    }
}
