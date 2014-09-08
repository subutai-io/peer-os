package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;

import com.google.common.base.Preconditions;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl implements CommandDispatcher {
    private static final Logger LOG = Logger.getLogger( CommandDispatcherImpl.class.getName() );

    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final DispatcherDAO dispatcherDAO;


    public CommandDispatcherImpl( final AgentManager agentManager, final CommandRunner commandRunner,
                                  final DbManager dbManager ) {
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.dispatcherDAO = new DispatcherDAO( dbManager );
    }


    public void init() {}


    public void destroy() {}


    public void sendRequests( final Map<UUID, Set<BatchRequest>> requests ) {

        //use PeerManager to figure out IP of target peer by UUID
        //check if peers are accessible otherwise throw RunCommandException
        //send requests in batch to each peer
    }


    public void processResponse( final Set<Response> responses ) {
        //trigger CommandRunner.onResponse
    }


    public void executeRequests( final UUID initiatorId, final Set<BatchRequest> requests ) {
        //execute requests using Command Runner
        //upon response in callback, cache response to persistent queue (key => initiator id)
        //some background thread should iterate this queue and attempt to send responses back to initiator
    }


    @Override
    public void runCommandAsync( final Command command, final CommandCallback commandCallback ) {
        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkArgument( command instanceof CommandImpl, "Command is of wrong type" );
        Preconditions.checkNotNull( commandCallback, "Callback is null" );

        final CommandImpl commandImpl = ( CommandImpl ) command;

        //send remote requests
        if ( !commandImpl.getRemoteRequests().isEmpty() ) {
            sendRequests( commandImpl.getRemoteRequests() );
        }

        //send local requests
        if ( !commandImpl.getRequests().isEmpty() ) {
            commandRunner.runCommandAsync( command, commandCallback );
        }
    }


    @Override
    public void runCommandAsync( final Command command ) {
        runCommandAsync( command, new CommandCallback() );
    }


    @Override
    public void runCommand( final Command command ) {
        runCommandAsync( command, new CommandCallback() );
        ( ( CommandImpl ) command ).waitCompletion();
    }


    @Override
    public void runCommand( final Command command, final CommandCallback commandCallback ) {
        runCommandAsync( command, commandCallback );
        ( ( CommandImpl ) command ).waitCompletion();
    }


    @Override
    public Command createCommand( final RequestBuilder requestBuilder, final Set<Agent> agents ) {
        return new CommandImpl( null, requestBuilder, agents );
    }


    @Override
    public Command createCommand( final String description, final RequestBuilder requestBuilder,
                                  final Set<Agent> agents ) {
        return new CommandImpl( description, requestBuilder, agents );
    }


    @Override
    public Command createCommand( final Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( null, agentRequestBuilders );
    }


    @Override
    public Command createCommand( final String description, final Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( description, agentRequestBuilders );
    }


    @Override
    public Command createBroadcastCommand( final RequestBuilder requestBuilder ) {
        throw new UnsupportedOperationException();
    }
}
