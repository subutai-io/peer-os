package org.safehaus.subutai.core.dispatcher.impl;


import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.safehaus.subutai.common.command.AgentRequestBuilder;
import org.safehaus.subutai.common.command.AgentResult;
import org.safehaus.subutai.common.command.CacheEntry;
import org.safehaus.subutai.common.command.Command;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandExecutor;
import org.safehaus.subutai.common.command.CommandExecutorExpiryCallback;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.ExpiringCache;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.BatchRequest;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.common.protocol.ResponseListener;
import org.safehaus.subutai.common.util.CollectionUtil;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DBException;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.core.dispatcher.api.CommandDispatcher;
import org.safehaus.subutai.core.dispatcher.api.RunCommandException;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Implementation of CommandDispatcher interface
 */
public class CommandDispatcherImpl implements CommandDispatcher, ResponseListener {
    private static final Logger LOG = Logger.getLogger( CommandDispatcherImpl.class.getName() );

    private final AgentManager agentManager;
    private final CommandRunner commandRunner;
    private final DispatcherDAO dispatcherDAO;
    private final ResponseSender responseSender;
    private final HttpUtil httpUtil;
    //cache of command executors where key is command UUID and value is CommandExecutor
    private ExpiringCache<UUID, CommandExecutor> commandExecutors;
    private UUID subutaiId;


    public CommandDispatcherImpl( final AgentManager agentManager, final CommandRunner commandRunner,
                                  final DbManager dbManager ) {
        this.agentManager = agentManager;
        this.commandRunner = commandRunner;
        this.dispatcherDAO = new DispatcherDAO( dbManager );
        this.httpUtil = new HttpUtil();
        this.responseSender = new ResponseSender( dispatcherDAO, httpUtil );
    }


    public void init() {
        commandExecutors = new ExpiringCache<>();
        responseSender.init();
    }


    public void destroy() {
        responseSender.dispose();
        httpUtil.dispose();
        Map<UUID, CacheEntry<CommandExecutor>> entries = commandExecutors.getEntries();
        //shutdown all executors which are still there
        for ( Map.Entry<UUID, CacheEntry<CommandExecutor>> entry : entries.entrySet() ) {
            try {
                entry.getValue().getValue().getExecutor().shutdown();
            }
            catch ( Exception ignore ) {
            }
        }
        commandExecutors.dispose();
    }


    public UUID getSubutaiUUID() {
        return subutaiId;
    }


    public void setSubutaiId( final String subutaiId ) {
        this.subutaiId = UUID.fromString( subutaiId );
    }


    private void sendRequests( final Map<UUID, Set<BatchRequest>> requests ) {
        //check if peers are accessible otherwise throw RunCommandException
        //not implemented yet...

        //check if agents are connected otherwise throw RunCommandException
        //not implemented yet...

        for ( Map.Entry<UUID, Set<BatchRequest>> request : requests.entrySet() ) {
            //use PeerManager to figure out IP of target peer by UUID
            //not implemented yet...
            String peerIP = getLocalIp();

            //send requests in batch to each peer
            Map<String, String> params = new HashMap<>();
            params.put( "ownerId", request.getKey().toString() );
            params.put( "requests", JsonUtil.toJson( request.getValue() ) );
            try {
                httpUtil.httpLitePost( String.format( Common.REQUEST_URL, peerIP ), params );
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

        for ( Response response : responses ) {
            onResponse( response );
        }
    }


    @Override
    public void executeRequests( final String ip, final UUID ownerId, final Set<BatchRequest> requests ) {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "IP is null or empty" );
        Preconditions.checkNotNull( ownerId, "Owner Id is null" );
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
                remoteRequest = new RemoteRequest( ip, ownerId, commandId, requestsCount );
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
                            dispatcherDAO.saveRemoteResponse( new RemoteResponse( ownerId, commandId, response ) );
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

        ExecutorService executor = Executors.newSingleThreadExecutor();
        CommandExecutor commandExecutor = new CommandExecutor( commandImpl, executor, commandCallback );

        //put command to cache
        boolean queued = commandExecutors.put( commandImpl.getCommandUUID(), commandExecutor,
                org.safehaus.subutai.common.settings.Common.INACTIVE_COMMAND_DROP_TIMEOUT_SEC * 1000 + 2000,
                new CommandExecutorExpiryCallback() );

        if ( queued ) {
            //set command status to RUNNING
            commandImpl.setCommandStatus( CommandStatus.RUNNING );
            //execute command

            //send remote requests
            if ( !commandImpl.getRemoteRequests().isEmpty() ) {
                sendRequests( commandImpl.getRemoteRequests() );
            }

            //send local requests
            if ( !commandImpl.getRequests().isEmpty() ) {
                Command localCommand = new CommandImpl( commandImpl.getRequests() );
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


    private String getLocalIp() {
        Enumeration<NetworkInterface> n = null;
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
        catch ( SocketException e ) {
        }


        return "172.16.192.64";
    }


    @Override
    public void onResponse( final Response response ) {
        if ( response != null && response.getUuid() != null && response.getTaskUuid() != null ) {
            final CommandExecutor commandExecutor = commandExecutors.get( response.getTaskUuid() );

            if ( commandExecutor != null ) {

                //process command response
                commandExecutor.getExecutor().execute( new Runnable() {

                    public void run() {
                        //obtain command lock
                        commandExecutor.getCommand().getUpdateLock();
                        try {
                            if ( commandExecutors.get( response.getTaskUuid() ) != null ) {

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
}
