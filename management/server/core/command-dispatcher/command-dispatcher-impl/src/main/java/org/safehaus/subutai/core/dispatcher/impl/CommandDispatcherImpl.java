package org.safehaus.subutai.core.dispatcher.impl;


import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Level;

import org.safehaus.subutai.common.command.AbstractCommandRunner;
import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandExecutor;
import org.safehaus.subutai.common.command.CommandExecutorExpiryCallback;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.dispatcher.api.RunCommandException;
import org.safehaus.subutai.core.peer.api.PeerManager;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl extends AbstractCommandRunner implements CommandDispatcher {

    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final DispatcherDAO dispatcherDAO;
    private final ResponseSender responseSender;
    private final HttpUtil httpUtil;
    private final PeerManager peerManager;


    public CommandDispatcherImpl( final AgentManager agentManager, final CommandRunner commandRunner,
                                  final DbManager dbManager, final PeerManager peerManager ) {
        super();
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.dispatcherDAO = new DispatcherDAO( dbManager );
        this.httpUtil = new HttpUtil();
        this.peerManager = peerManager;
        this.responseSender = new ResponseSender( dispatcherDAO, httpUtil );
    }


    public void init() {
        responseSender.init();
    }


    public void destroy() {
        responseSender.dispose();
        httpUtil.dispose();
        super.dispose();
    }


    private void executeCommand( CommandImpl command ) {
        //TODO check if peers are accessible otherwise throw RunCommandException
        //not implemented yet...

        //TODO check if agents are connected otherwise throw RunCommandException
        //not implemented yet...

        //send remote requests
        if ( !command.getRemoteRequests().isEmpty() ) {
            LOG.warning( "executing remote requests" );
            sendRequests( command.getRemoteRequests() );
        }

        //send local requests
        if ( !command.getRequests().isEmpty() ) {
            LOG.warning( "executing local requests" );
            Command localCommand = new CommandImpl( command.getRequests() );
            final CommandDispatcherImpl self = this;
            commandRunner.runCommandAsync( localCommand, new CommandCallback() {
                @Override
                public void onResponse( final Response response, final AgentResult agentResult,
                                        final Command command ) {
                    self.onResponse( response );
                }
            } );
        }
    }


    private void sendRequests( final Map<UUID, Set<BatchRequest>> requests ) {

        for ( Map.Entry<UUID, Set<BatchRequest>> request : requests.entrySet() ) {
            //TODO use PeerManager to figure out IP of target peer by UUID
            //not implemented yet...
            String peerIP = getLocalIp();

            //send requests in batch to each peer
            Map<String, String> params = new HashMap<>();
            params.put( "requests", JsonUtil.toJson( request.getValue() ) );
            try {
                httpUtil.post( String.format( Common.REQUEST_URL, peerIP ), params );
            }
            catch ( IOException e ) {
                String errString =
                        String.format( "Error in sendRequests for peer %s: %s", request.getKey(), e.getMessage() );
                LOG.log( Level.SEVERE, errString );

                throw new RunCommandException( errString );
            }
        }
    }


    @Override
    public void processResponses( final Set<Response> responses ) {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( responses ), "Responses are null or empty" );

        Set<Response> sortedSet = new TreeSet<>( new Comparator<Response>() {

            @Override
            public int compare( final Response o1, final Response o2 ) {
                int compareAgents = o1.getUuid().compareTo( o2.getUuid() );
                return compareAgents == 0 ? o1.getResponseSequenceNumber().compareTo( o2.getResponseSequenceNumber() ) :
                       compareAgents;
            }
        } );
        sortedSet.addAll( responses );
        for ( Response response : sortedSet ) {
            onResponse( response );
        }
    }


    @Override
    public void executeRequests( final String ip, final Set<BatchRequest> requests ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "IP is null or empty" );
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( requests ), "Requests are empty or null" );

        try {
            int requestsCount = 0;
            for ( BatchRequest batchRequest : requests ) {
                requestsCount += batchRequest.getRequestsCount();
            }
            final UUID commandId = requests.iterator().next().getCommandId();
            //get request from db
            RemoteRequest remoteRequest = dispatcherDAO.getRemoteRequest( commandId );
            //if no request exists, create a new one
            if ( remoteRequest == null ) {
                remoteRequest = new RemoteRequest( ip, commandId, requestsCount );
                //save request to db
                dispatcherDAO.saveRemoteRequest( remoteRequest );

                //execute requests using Command Runner
                Command command = new CommandImpl( requests );
                commandRunner.runCommandAsync( command, new CommandCallback() {
                    @Override
                    public void onResponse( final Response response, final AgentResult agentResult,
                                            final Command command ) {
                        try {

                            //save response to db
                            dispatcherDAO.saveRemoteResponse( new RemoteResponse( response ) );
                        }
                        catch ( DBException e ) {
                            LOG.log( Level.SEVERE,
                                    String.format( "Error in executeRequests: [%s] for response: %s", e.getMessage(),
                                            response ) );
                            throw new RunCommandException( e.getMessage() );
                        }
                    }
                } );
            }
            else {
                throw new RunCommandException(
                        String.format( "Command %s is already queued for processing", commandId ) );
            }
        }
        catch ( DBException e ) {
            LOG.log( Level.SEVERE, String.format( "Error in executeRequests: [%s]", e.getMessage() ) );
            throw new RunCommandException( e.getMessage() );
        }
    }


    @Override
    public void runCommandAsync( final Command command, final CommandCallback commandCallback ) {
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

        if ( queued ) {
            //set command status to RUNNING
            commandImpl.setCommandStatus( CommandStatus.RUNNING );

            //execute command
            executeCommand( commandImpl );
        }
        else {
            throw new RunCommandException( "Could not queue command for processing" );
        }
    }


    @Override
    public Command createCommand( final RequestBuilder requestBuilder, final Set<Agent> agents ) {
        return new CommandImpl( null, requestBuilder, agents, peerManager );
    }


    @Override
    public Command createCommand( final String description, final RequestBuilder requestBuilder,
                                  final Set<Agent> agents ) {
        return new CommandImpl( description, requestBuilder, agents, peerManager );
    }


    @Override
    public Command createCommand( final Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( null, agentRequestBuilders, peerManager );
    }


    @Override
    public Command createCommand( final String description, final Set<AgentRequestBuilder> agentRequestBuilders ) {
        return new CommandImpl( description, agentRequestBuilders, peerManager );
    }


    private String getLocalIp() {
        Enumeration<NetworkInterface> n;
        try {
            n = NetworkInterface.getNetworkInterfaces();
            for (; n.hasMoreElements(); ) {
                NetworkInterface e = n.nextElement();

                Enumeration<InetAddress> a = e.getInetAddresses();
                for (; a.hasMoreElements(); ) {
                    InetAddress addr = a.nextElement();
                    if ( addr.getHostAddress().startsWith( "172" ) ) {
                        return addr.getHostAddress();
                    }
                }
            }
        }
        catch ( SocketException ignore ) {
        }


        return "172.16.192.64";
    }
}
