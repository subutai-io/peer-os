package org.safehaus.subutai.core.dispatcher.impl;


import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Container;
import org.safehaus.subutai.common.protocol.Request;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.command.api.command.AbstractCommandRunner;
import org.safehaus.subutai.core.command.api.command.AgentRequestBuilder;
import org.safehaus.subutai.core.command.api.command.AgentResult;
import org.safehaus.subutai.core.command.api.command.Command;
import org.safehaus.subutai.core.command.api.command.CommandCallback;
import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.core.command.api.command.CommandExecutor;
import org.safehaus.subutai.core.command.api.command.CommandExecutorExpiryCallback;
import org.safehaus.subutai.common.protocol.CommandStatus;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.dispatcher.api.ContainerRequestBuilder;
import org.safehaus.subutai.core.dispatcher.api.RunCommandException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.message.PeerMessageException;
import org.safehaus.subutai.core.peer.api.message.PeerMessageListener;

import com.google.common.base.Preconditions;
import com.google.gson.JsonSyntaxException;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl extends AbstractCommandRunner implements CommandDispatcher, PeerMessageListener
{

    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final DispatcherDAO dispatcherDAO;
    private final ResponseSender responseSender;
    private final PeerManager peerManager;


    public CommandDispatcherImpl( final AgentManager agentManager, final CommandRunner commandRunner,
                                  final PeerManager peerManager, final DataSource dataSource ) throws DaoException
    {
        super();

        Preconditions.checkNotNull( agentManager, "Agent Manager is null" );
        Preconditions.checkNotNull( commandRunner, "Command Runner is null" );
        Preconditions.checkNotNull( peerManager, "Peer Manager is null" );
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.dispatcherDAO = new DispatcherDAO( dataSource );
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.peerManager = peerManager;
        this.responseSender = new ResponseSender( dispatcherDAO, peerManager );
    }


    public void init()
    {

        peerManager.addPeerMessageListener( this );
        responseSender.init();
    }


    public void destroy()
    {
        peerManager.removePeerMessageListener( this );
        responseSender.dispose();
        super.dispose();
    }


    @Override
    public void runCommandAsync( final Command command, final CommandCallback commandCallback )
    {
        Preconditions.checkNotNull( command, "Command is null" );
        Preconditions.checkArgument( command instanceof CommandImpl, "Command is of wrong type" );
        Preconditions.checkNotNull( commandCallback, "Callback is null" );

        final CommandImpl commandImpl = ( CommandImpl ) command;

        Preconditions.checkArgument( commandExecutors.get( commandImpl.getCommandUUID() ) == null,
                "" + "This command has been already queued for execution" );
        Preconditions
                .checkArgument( !( commandImpl.getRequests().isEmpty() && commandImpl.getRemoteRequests().isEmpty() ),
                        "Requests are empty" );

        CommandExecutor commandExecutor = new CommandExecutor( commandImpl, commandCallback );

        //put command to cache
        boolean queued = commandExecutors.put( commandImpl.getCommandUUID(), commandExecutor,
                org.safehaus.subutai.common.settings.Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000 + 2000,
                new CommandExecutorExpiryCallback() );

        if ( queued )
        {
            //set command status to RUNNING
            commandImpl.setCommandStatus( CommandStatus.RUNNING );

            //execute command
            executeCommand( commandImpl );
        }
        else
        {
            throw new RunCommandException( "Could not queue command for processing" );
        }
    }


    private void executeCommand( CommandImpl command )
    {
        checkRemoteConnectivity( command );
        checkIfLocalAgentsConnected( command );

        //send remote requests
        if ( !command.getRemoteRequests().isEmpty() )
        {
            LOG.warn( "executing remote requests" );

            sendRequests( command.getRemoteRequests() );
        }

        //send local requests
        if ( !command.getRequests().isEmpty() )
        {

            LOG.warn( "executing local requests" );

            Command localCommand = new CommandImpl( command.getRequests(), commandRunner );
            final CommandDispatcherImpl self = this;
            try
            {
                localCommand.executeAsync( new CommandCallback()
                {
                    @Override
                    public void onResponse( final Response response, final AgentResult agentResult,
                                            final Command command )
                    {
                        self.onResponse( response );
                    }
                } );
            }
            catch ( CommandException e )
            {
                LOG.error( "Error executing local requests", e );
            }
        }
    }


    private void checkRemoteConnectivity( CommandImpl command )
    {
        for ( Map.Entry<UUID, Set<BatchRequest>> entry : command.getRemoteRequests().entrySet() )
        {
            //check if peer is registered
            Peer peer = peerManager.getPeerByUUID( entry.getKey() );
            if ( peer == null )
            {
                throw new RunCommandException( String.format( "Peer %s not found", entry.getKey() ) );
            }
            //check if batch requests are not empty
            if ( CollectionUtil.isCollectionEmpty( entry.getValue() ) )
            {
                throw new RunCommandException( "Batch requests are empty" );
            }
            try
            {
                //check if remote peers are reachable
                if ( !peerManager.isPeerReachable( peer ) )
                {
                    throw new RunCommandException( String.format( "Peer is not reachable %s", peer ) );
                }
                else
                {
                    checkIfRemoteAgentsConnected( peer, entry.getValue() );
                }
            }
            catch ( PeerException e )
            {
                String err = String.format( "Error in executeCommand: %s", e.getMessage() );
                LOG.error( err, e );
                throw new RunCommandException( err );
            }
        }
    }


    private void checkIfRemoteAgentsConnected( Peer peer, Set<BatchRequest> requests ) throws PeerException
    {
        //check if all agents required for command execution are connected on remote peer
        UUID environmentId = requests.iterator().next().getEnvironmentId();

        Set<Agent> agents = peerManager.getConnectedAgents( peer, environmentId.toString() );

        Set<UUID> connectedAgentsIds = new HashSet<>();
        for ( Agent agent : agents )
        {
            connectedAgentsIds.add( agent.getUuid() );
        }
        Set<UUID> requestAgentsIds = new HashSet<>();
        for ( BatchRequest batchRequest : requests )
        {
            requestAgentsIds.addAll( batchRequest.getAgentIds() );
        }

        if ( !connectedAgentsIds.containsAll( requestAgentsIds ) )
        {
            CollectionUtil.removeValues( requestAgentsIds, connectedAgentsIds );
            throw new RunCommandException( String.format( "Agents %s are not connected", requestAgentsIds ) );
        }
    }


    private void checkIfLocalAgentsConnected( CommandImpl command )
    {
        //check if local agents are connected
        Set<UUID> localIds = new HashSet<>();
        for ( Request request : command.getRequests() )
        {
            if ( agentManager.getAgentByUUID( request.getUuid() ) == null )
            {
                localIds.add( request.getUuid() );
            }
        }
        if ( !localIds.isEmpty() )
        {
            throw new RunCommandException( String.format( "Agents %s are not connected", localIds ) );
        }
    }


    private void sendRequests( final Map<UUID, Set<BatchRequest>> requests )
    {

        for ( Map.Entry<UUID, Set<BatchRequest>> request : requests.entrySet() )
        {
            Peer peer = peerManager.getPeerByUUID( request.getKey() );

            try
            {
                String message =
                        JsonUtil.toJson( new DispatcherMessage( DispatcherMessageType.REQUEST, request.getValue() ) );
                peerManager.sendPeerMessage( peer, Common.DISPATCHER_NAME, message );
            }
            catch ( JsonSyntaxException | PeerMessageException e )
            {
                String errString = String.format( "Error in sendRequests for peer %s: %s", peer, e.getMessage() );

                LOG.error( errString, e );
                throw new RunCommandException( errString );
            }
        }
    }


    @Override
    public Command createContainerCommand( final RequestBuilder requestBuilder, final Set<Container> containers )
    {
        return new CommandImpl( requestBuilder, containers, peerManager, this );
    }


    @Override
    public Command createContainerCommand( final Set<ContainerRequestBuilder> requestBuilders )
    {
        return new CommandImpl( requestBuilders, peerManager, this );
    }


    @Override
    public Command createCommand( final RequestBuilder requestBuilder, final Set<Agent> agents )
    {
        return createCommand( null, requestBuilder, agents );
    }


    @Override
    public Command createCommand( final String description, final RequestBuilder requestBuilder,
                                  final Set<Agent> agents )
    {
        return new CommandImpl( description, requestBuilder, agents, peerManager, this );
    }


    @Override
    public Command createCommand( final Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return createCommand( null, agentRequestBuilders );
    }


    @Override
    public Command createCommand( final String description, final Set<AgentRequestBuilder> agentRequestBuilders )
    {
        return new CommandImpl( description, agentRequestBuilders, peerManager, this );
    }


    @Override
    public String onMessage( final Peer peer, final String peerMessage ) throws PeerMessageException
    {
        try
        {

            DispatcherMessage dispatcherMessage = JsonUtil.fromJson( peerMessage, DispatcherMessage.class );


            if ( dispatcherMessage.getDispatcherMessageType() == DispatcherMessageType.REQUEST )
            {
                executeRequests( peer, dispatcherMessage.getBatchRequests() );
            }
            else
            {
                processResponses( dispatcherMessage.getResponses() );
            }
        }
        catch ( RuntimeException e )
        {
            LOG.error( "Error in onMessage", e );
            throw new PeerMessageException( e.getMessage() );
        }
        return null;
    }


    private void processResponses( final Set<Response> responses )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( responses ), "Responses are null or empty" );

        Set<Response> sortedSet = new TreeSet<>( new Comparator<Response>()
        {

            @Override
            public int compare( final Response o1, final Response o2 )
            {
                int compareAgents = o1.getUuid().compareTo( o2.getUuid() );
                return compareAgents == 0 ? o1.getResponseSequenceNumber().compareTo( o2.getResponseSequenceNumber() ) :
                       compareAgents;
            }
        } );
        sortedSet.addAll( responses );
        for ( Response response : sortedSet )
        {
            onResponse( response );
        }
    }


    private void executeRequests( final Peer peer, final Set<BatchRequest> requests ) throws PeerMessageException
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requests ), "Requests are empty or null" );

        try
        {
            int requestsCount = 0;
            for ( BatchRequest batchRequest : requests )
            {
                requestsCount += batchRequest.getRequestsCount();
            }
            final UUID commandId = requests.iterator().next().getCommandId();
            //get request from db
            RemoteRequest remoteRequest = dispatcherDAO.getRemoteRequest( commandId );
            //if no request exists, create a new one
            if ( remoteRequest == null )
            {
                remoteRequest = new RemoteRequest( peer.getId(), commandId, requestsCount );
                //save request to db
                dispatcherDAO.saveRemoteRequest( remoteRequest );

                //execute requests using Command Runner
                Command command = new CommandImpl( requests, commandRunner );
                command.executeAsync( new CommandCallback()
                {
                    @Override
                    public void onResponse( final Response response, final AgentResult agentResult,
                                            final Command command )
                    {
                        saveResponse( response );
                    }
                } );
            }
            else
            {
                throw new PeerMessageException(
                        String.format( "Command %s is already queued for processing", commandId ) );
            }
        }
        catch ( CommandException | DaoException e )
        {
            LOG.error( String.format( "Error in executeRequests: [%s]", e.getMessage() ), e );
            throw new PeerMessageException( e.getMessage() );
        }
    }


    protected void saveResponse( Response response )
    {
        try
        {
            //save response to db
            dispatcherDAO.saveRemoteResponse( new RemoteResponse( response ) );
        }
        catch ( NullPointerException | DaoException e )
        {
            LOG.error( String.format( "Error in executeRequests: [%s] for response: %s", e.getMessage(), response ),
                    e );
            throw new RunCommandException( e.getMessage() );
        }
    }


    @Override
    public String getName()
    {
        return Common.DISPATCHER_NAME;
    }
}
