/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.command.impl;


import java.util.Set;

import org.safehaus.subutai.common.exception.RunCommandException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.settings.Common;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AbstractCommand;
import org.safehaus.subutai.core.command.api.command.AbstractCommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.core.command.api.command.CommandExecutor;
import org.safehaus.subutai.core.command.api.command.CommandExecutorExpiryCallback;
import org.safehaus.subutai.core.communication.api.CommunicationManager;

import com.google.common.base.Preconditions;


/**
 * This class is an implementation of CommandRunner interface. Runs commands on agents and routes received responses to
 * corresponding callbacks.
 */
public class CommandRunnerImpl extends AbstractCommandRunner implements CommandRunner
{

    private final CommunicationManager communicationManager;
    private final AgentManager agentManager;
    private int inactiveCommandDropTimeout = Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC;


    public CommandRunnerImpl( CommunicationManager communicationManager, AgentManager agentManager )
    {
        super();

        Preconditions.checkNotNull( communicationManager, "Communication Manager is null" );
        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );

        this.communicationManager = communicationManager;
        this.agentManager = agentManager;
    }


    protected void setInactiveCommandDropTimeout( final int inactiveCommandDropTimeout )
    {
        this.inactiveCommandDropTimeout = inactiveCommandDropTimeout;
    }


    /**
     * Initialized command runner
     */
    public void init()
    {
        communicationManager.addListener( this );
    }


    /**
     * Disposes command runner
     */
    public void destroy()
    {
        communicationManager.removeListener( this );
        super.dispose();
    }


    /**
     * Returns broadcast command for the supplied request
     *
     * @param requestBuilder - request builder
     *
     * @return - {@code Command}
     */
    @Override
    public Command createBroadcastCommand( RequestBuilder requestBuilder )
    {
        Set<Agent> agents = agentManager.getAgents();
        return new CommandImpl( requestBuilder, agents.size(), this );
    }


    /**
     * Runs command asynchronously to the calling party. Result of command can be checked later using the associated
     * command object
     *
     * @param command - command to run
     * @param commandCallback - callback to trigger on every response
     */
    @Override
    public void runCommandAsync( final Command command, CommandCallback commandCallback )
    {
        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkArgument( command instanceof AbstractCommand, "Command is of wrong type" );
        Preconditions.checkNotNull( commandCallback, "Callback is null" );

        final AbstractCommand commandImpl = ( AbstractCommand ) command;
        Preconditions.checkArgument( commandExecutors.get( commandImpl.getCommandUUID() ) == null,
                "" + "This command has been already queued for execution" );
        Preconditions.checkArgument( !commandImpl.getRequests().isEmpty(), "Requests are empty" );

        CommandExecutor commandExecutor = new CommandExecutor( commandImpl, commandCallback );

        //put command to cache
        boolean queued = commandExecutors
                .put( commandImpl.getCommandUUID(), commandExecutor, inactiveCommandDropTimeout * 1000,
                        new CommandExecutorExpiryCallback() );

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
        else
        {
            throw new RunCommandException( "Could not queue command for processing" );
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
        return createCommand( null, requestBuilder, agents );
    }


    /**
     * Creates command using supplied request for the supplied set of agents
     *
     * @param description - command description
     * @param requestBuilder - request builder
     * @param agents - target agents
     */
    @Override
    public Command createCommand( String description, RequestBuilder requestBuilder, Set<Agent> agents )
    {
        return new CommandImpl( description, requestBuilder, agents, this );
    }


    /**
     * Creates command using supplied agent request builders
     *
     * @param agentRequestBuilders - agent request builders
     */
    @Override
    public Command createCommand( Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return createCommand( null, agentRequestBuilders );
    }


    /**
     * Creates command using supplied agent request builders
     *
     * @param description - command description
     * @param agentRequestBuilders - agent request builders
     */
    @Override
    public Command createCommand( String description, Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return new CommandImpl( description, agentRequestBuilders, this );
    }
}
