/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AbstractCommand;
import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.CacheEntry;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandExecutor;
import org.safehaus.subutai.common.command.CommandExecutorExpiryCallback;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.ExpiringCache;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.communication.api.CommunicationManager;

import com.google.common.base.Preconditions;


/**
 * This class is an implementation of CommandRunner interface. Runs commands on agents and routes received responses to
 * corresponding callbacks.
 */
public class CommandRunnerImpl implements CommandRunner
{

    private static final Logger LOG = Logger.getLogger( CommandRunnerImpl.class.getName() );

    private final CommunicationManager communicationManager;
    private final AgentManager agentManager;
    //cache of command executors where key is command UUID and value is CommandExecutor
    private ExpiringCache<UUID, CommandExecutor> commandExecutors;


    public CommandRunnerImpl( CommunicationManager communicationManager, AgentManager agentManager )
    {
        Preconditions.checkNotNull( communicationManager, "Communication Manager is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.communicationManager = communicationManager;
        this.agentManager = agentManager;
    }


    /**
     * Initialized command runner
     */
    public void init()
    {
        communicationManager.addListener( this );
        commandExecutors = new ExpiringCache<>();
    }


    /**
     * Disposes command runner
     */
    public void destroy()
    {
        communicationManager.removeListener( this );
        Map<UUID, CacheEntry<CommandExecutor>> entries = commandExecutors.getEntries();
        //shutdown all executors which are still there
        for ( Map.Entry<UUID, CacheEntry<CommandExecutor>> entry : entries.entrySet() )
        {
            try
            {
                entry.getValue().getValue().getExecutor().shutdown();
            }
            catch ( Exception ignore )
            {
            }
        }
        commandExecutors.dispose();
    }


    /**
     * Receives all responses from agents. Triggered by communication manager
     *
     * @param response - received response
     */
    public void onResponse( final Response response )
    {
        if ( response != null && response.getUuid() != null && response.getTaskUuid() != null )
        {
            final CommandExecutor commandExecutor = commandExecutors.get( response.getTaskUuid() );

            if ( commandExecutor != null )
            {

                //process command response
                commandExecutor.getExecutor().execute( new Runnable()
                {

                    public void run()
                    {
                        //obtain command lock
                        commandExecutor.getCommand().getUpdateLock();
                        try
                        {
                            if ( commandExecutors.get( response.getTaskUuid() ) != null )
                            {

                                //append results to command
                                commandExecutor.getCommand().appendResult( response );

                                //call command callback
                                try
                                {
                                    commandExecutor.getCallback().onResponse( response,
                                            commandExecutor.getCommand().getResults().get( response.getUuid() ),
                                            commandExecutor.getCommand() );
                                }
                                catch ( Exception e )
                                {
                                    LOG.log( Level.SEVERE, "Error in callback {0}", e );
                                }

                                //do cleanup on command completion or interruption by user
                                if ( commandExecutor.getCommand().hasCompleted() || commandExecutor.getCallback()
                                                                                                   .isStopped() )
                                {
                                    //remove command executor so that
                                    //if response comes from agent it is not processed by callback
                                    commandExecutors.remove( commandExecutor.getCommand().getCommandUUID() );
                                    //call this to notify all waiting threads that command completed
                                    commandExecutor.getCommand().notifyWaitingThreads();
                                    //shutdown command executor
                                    commandExecutor.getExecutor().shutdown();
                                }
                            }
                        }
                        finally
                        {
                            commandExecutor.getCommand().releaseUpdateLock();
                        }
                    }
                } );
            }
        }
    }


    /**
     * Returns broadcast command for the supplied request
     *
     * @param requestBuilder - request builder
     *
     * @return - {@code Command}
     */
    public Command createBroadcastCommand( RequestBuilder requestBuilder )
    {
        Set<Agent> agents = agentManager.getAgents();
        return new CommandImpl( requestBuilder, agents.size() );
    }


    /**
     * Runs command asynchronously to the calling party. Result of command can be checked later using the associated
     * command object
     *
     * @param command - command to run
     * @param commandCallback - callback to trigger on every response
     */
    public void runCommandAsync( final Command command, CommandCallback commandCallback )
    {
        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkArgument( command instanceof AbstractCommand, "Command is of wrong type" );
        Preconditions.checkNotNull( commandCallback, "Callback is null" );

        final AbstractCommand commandImpl = ( AbstractCommand ) command;
        Preconditions.checkArgument( commandExecutors.get( commandImpl.getCommandUUID() ) == null,
                "" + "This command has been already queued for execution" );
        Preconditions.checkArgument( !commandImpl.getRequests().isEmpty(), "Requests are empty" );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CommandExecutor commandExecutor = new CommandExecutor( commandImpl, executor, commandCallback );

        //put command to cache
        boolean queued = commandExecutors.put( commandImpl.getCommandUUID(), commandExecutor,
                Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000 + 2000, new CommandExecutorExpiryCallback() );

        if ( queued )
        {
            //set command status to RUNNING
            commandImpl.setCommandStatus( CommandStatus.RUNNING );
            //execute command
            if ( commandImpl.isBroadcastCommand() )
            {
                communicationManager.sendBroadcastRequest( commandImpl.getRequests().iterator().next() );
            }
            else
            {
                for ( Request request : commandImpl.getRequests() )
                {
                    communicationManager.sendRequest( request );
                }
            }
        }
    }


    /**
     * Creates command using supplied request for the supplied set of agents
     *
     * @param requestBuilder - request builder
     * @param agents - target agents
     */
    public Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents )
    {
        return new CommandImpl( null, requestBuilder, agents );
    }


    /**
     * Creates command using supplied request for the supplied set of agents
     *
     * @param description - command description
     * @param requestBuilder - request builder
     * @param agents - target agents
     */
    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents )
    {
        return new CommandImpl( description, requestBuilder, agents );
    }


    /**
     * Creates command using supplied agent request builders
     *
     * @param agentRequestBuilders - agent request builders
     */
    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return new CommandImpl( null, agentRequestBuilders );
    }


    /**
     * Creates command using supplied agent request builders
     *
     * @param description - command description
     * @param agentRequestBuilders - agent request builders
     */
    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return new CommandImpl( description, agentRequestBuilders );
    }


    /**
     * Runs command synchronously. Call returns after final response is received or stop() method is called from inside
     * a callback
     *
     * @param command - command to run
     * @param commandCallback - - callback to trigger on every response
     */
    public void runCommand( Command command, CommandCallback commandCallback )
    {
        runCommandAsync( command, commandCallback );
        ( ( CommandImpl ) command ).waitCompletion();
    }


    /**
     * Runs command asynchronously to the calling party. Result of command can be checked later using the associated
     * command object
     *
     * @param command - command to run
     */
    public void runCommandAsync( Command command )
    {
        runCommandAsync( command, new CommandCallback() );
    }


    /**
     * Runs command synchronously. Call returns after final response is received
     *
     * @param command - command to run
     */
    public void runCommand( Command command )
    {
        runCommandAsync( command, new CommandCallback() );
        ( ( CommandImpl ) command ).waitCompletion();
    }
}
