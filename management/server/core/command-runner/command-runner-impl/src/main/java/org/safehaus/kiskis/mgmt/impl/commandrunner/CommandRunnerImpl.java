/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.commandrunner;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.kiskis.mgmt.api.commandrunner.AgentRequestBuilder;
import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandCallback;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandRunner;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandStatus;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import com.google.common.base.Preconditions;


/**
 * This class is implementation of CommandRunner interface. Runs commands on agents and routes received responses to
 * corresponding callbacks.
 */
public class CommandRunnerImpl implements CommandRunner, ResponseListener {

    private static final Logger LOG = Logger.getLogger( CommandRunnerImpl.class.getName() );

    private final CommunicationManager communicationManager;
    //cache of command executors where key is command UUID and value is CommandExecutor
    private ExpiringCache<UUID, CommandExecutor> commandExecutors;


    public CommandRunnerImpl( CommunicationManager communicationManager ) {
        Preconditions.checkNotNull( communicationManager, "Communication Manager is null" );

        this.communicationManager = communicationManager;
    }


    /**
     * Initialized command runner
     */
    public void init() {
        communicationManager.addListener( this );
        commandExecutors = new ExpiringCache<>();
    }


    /**
     * Disposes command runner
     */
    public void destroy() {
        communicationManager.removeListener( this );
        Map<UUID, CacheEntry<CommandExecutor>> entries = commandExecutors.getEntries();
        //shutdown all executors which are still there
        for ( Map.Entry<UUID, CacheEntry<CommandExecutor>> entry : entries.entrySet() ) {
            try {
                entry.getValue().getValue().getExecutor().shutdown();
            }
            catch ( Exception e ) {
            }
        }
        commandExecutors.dispose();
    }


    /**
     * Receives all responses from agents. Triggered by communication manager
     *
     * @param response - received response
     */
    public void onResponse( final Response response ) {
        if ( response != null && response.getUuid() != null ) {
            final CommandExecutor commandExecutor = commandExecutors.get( response.getTaskUuid() );

            if ( commandExecutor != null ) {

                //process command response
                commandExecutor.getExecutor().execute( new Runnable() {

                    public void run() {
                        //obtain command lock
                        commandExecutor.getCommand().getUpdateLock();
                        try {
                            if ( commandExecutors.get( response.getTaskUuid() ) != null ) {
                                //reset ttl of CommandExecutor

                                //append results to command
                                commandExecutor.getCommand().appendResult( response );

                                //call command callback
                                try {
                                    commandExecutor.getCallback().onResponse( response,
                                            commandExecutor.getCommand().getResults().get( response.getUuid() ),
                                            commandExecutor.getCommand() );
                                }
                                catch ( Exception e ) {
                                    LOG.log( Level.SEVERE, "Error in callback {0}", e );
                                }

                                //do cleanup on command completion or interruption by user
                                if ( commandExecutor.getCommand().hasCompleted() || commandExecutor.getCallback()
                                                                                                   .isStopped() ) {
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
                        finally {
                            commandExecutor.getCommand().releaseUpdateLock();
                        }
                    }
                } );
            }
        }
    }


    public void runCommandAsync( final Command command, CommandCallback commandCallback ) {
        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkArgument( command instanceof CommandImpl, "Command is of wrong type" );
        Preconditions.checkNotNull( commandCallback, "Callback is null" );
        final CommandImpl commandImpl = ( CommandImpl ) command;
        Preconditions.checkArgument( commandExecutors.get( commandImpl.getCommandUUID() ) == null,
                "" + "This command has been already queued for execution" );
        Preconditions.checkArgument( commandImpl.getRequests() != null && !commandImpl.getRequests().isEmpty(),
                "Requests are null or empty" );

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CommandExecutor commandExecutor = new CommandExecutor( commandImpl, executor, commandCallback );

        //put command to cache
        boolean queued = commandExecutors.put( commandImpl.getCommandUUID(), commandExecutor,
                //                        commandImpl.getTimeout() * 1000 + 2000,
                Common.MAX_COMMAND_TIMEOUT_SEC * 1000 + 2000, new CommandExecutorExpiryCallback() );

        if ( queued ) {
            //set command status to RUNNING
            commandImpl.setCommandStatus( CommandStatus.RUNNING );
            //execute command
            for ( Request request : commandImpl.getRequests() ) {
                communicationManager.sendRequest( request );
            }
        }
    }


    public Command createCommand( RequestBuilder requestBuilder, Set<Agent> agents ) {
        return new CommandImpl( null, requestBuilder, agents );
    }


    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents ) {
        return new CommandImpl( description, requestBuilder, agents );
    }


    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( null, agentRequestBuilders );
    }


    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( description, agentRequestBuilders );
    }


    public void runCommand( Command command, CommandCallback commandCallback ) {
        runCommandAsync( command, commandCallback );
        ( ( CommandImpl ) command ).waitCompletion();
    }


    public void runCommandAsync( Command command ) {
        runCommandAsync( command, new CommandCallback() );
    }


    public void runCommand( Command command ) {
        runCommandAsync( command, new CommandCallback() );
        ( ( CommandImpl ) command ).waitCompletion();
    }
}
