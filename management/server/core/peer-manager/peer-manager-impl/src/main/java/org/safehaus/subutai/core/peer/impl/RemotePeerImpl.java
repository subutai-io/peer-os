package org.safehaus.subutai.core.peer.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.CommandStatus;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.protocol.Criteria;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.HostKey;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.peer.impl.command.BlockingCommandCallback;
import org.safehaus.subutai.core.peer.impl.command.CommandRequest;
import org.safehaus.subutai.core.peer.impl.command.CommandResponseListener;
import org.safehaus.subutai.core.peer.impl.command.CommandResultImpl;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerRequest;
import org.safehaus.subutai.core.peer.impl.container.CreateContainerResponse;
import org.safehaus.subutai.core.peer.impl.model.ContainerHostEntity;
import org.safehaus.subutai.core.peer.impl.request.MessageRequest;
import org.safehaus.subutai.core.peer.impl.request.MessageResponse;
import org.safehaus.subutai.core.peer.impl.request.MessageResponseListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Remote Peer implementation
 */
public class RemotePeerImpl implements RemotePeer
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerImpl.class.getName() );

    private LocalPeer localPeer;
    protected PeerInfo peerInfo;
    protected Messenger messenger;

    private CommandResponseListener commandResponseListener;
    private MessageResponseListener messageResponseListener;


    public RemotePeerImpl( LocalPeer localPeer, final PeerInfo peerInfo, final Messenger messenger,
                           CommandResponseListener commandResponseListener,
                           MessageResponseListener messageResponseListener )
    {
        this.localPeer = localPeer;
        this.peerInfo = peerInfo;
        this.messenger = messenger;
        this.commandResponseListener = commandResponseListener;
        this.messageResponseListener = messageResponseListener;
    }


    @Override
    public UUID getId()
    {
        return peerInfo.getId();
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        if ( peerInfo.getId().equals( getRemoteId() ) )
        {
            return true;
        }
        else
        {
            throw new PeerException( "Invalid peer ID." );
        }
    }


    @Override
    public UUID getRemoteId() throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 10000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.getId();
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return peerInfo.getOwnerId();
    }


    @Override
    public PeerInfo getPeerInfo()
    {
        return peerInfo;
    }


    @Override
    public Set<ContainerHost> getContainerHostsByEnvironmentId( final UUID environmentId ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.getContainerHostsByEnvironmentId( environmentId );
    }


    @Override
    public Set<ContainerHost> createContainers( final UUID creatorPeerId, final UUID environmentId,
                                                final List<Template> templates, final int quantity,
                                                final String strategyId, final List<Criteria> criteria,
                                                String nodeGroupName ) throws PeerException
    {
        try
        {
            //send create request
            CreateContainerRequest request =
                    new CreateContainerRequest( creatorPeerId, environmentId, templates, quantity, strategyId, criteria,
                            nodeGroupName );

            CreateContainerResponse response = sendRequest( request, RecipientType.CONTAINER_CREATE_REQUEST.name(),
                    Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT, CreateContainerResponse.class );

            if ( response != null )
            {
                Set<HostKey> hostKeys = response.getHostKeys();
                Set<ContainerHost> result = getContainerHostImpl( hostKeys );
                return result;
            }
            else
            {
                throw new PeerException( "Received null response" );
            }
        }
        catch ( PeerException e )
        {
            LOG.error( "Error in createContainers", e );
            throw new PeerException( e.getMessage() );
        }
    }


    private Set<ContainerHost> getContainerHostImpl( final Set<HostKey> hostKeys )
    {
        Set<ContainerHost> result = new HashSet<>();
        for ( HostKey hostKey : hostKeys )
        {
            result.add( new ContainerHostEntity( hostKey ) );
        }
        return result;
    }


    @Override
    public void startContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        remotePeerRestClient.startContainer( containerHost );
    }


    @Override
    public void stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        remotePeerRestClient.stopContainer( containerHost );
    }


    @Override
    public void destroyContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        remotePeerRestClient.destroyContainer( containerHost );
    }


    @Override
    public boolean isConnected( final Host host ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 10000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.isConnected( host );
    }


    @Override
    public String getQuota( final ContainerHost host, final QuotaEnum quota ) throws PeerException
    {
        throw new PeerException( "Operation not allowed." );
    }


    @Override
    public void setQuota( final ContainerHost host, final QuotaEnum quota, final String value ) throws PeerException
    {
        throw new PeerException( "Operation not allowed." );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        return execute( requestBuilder, host, null );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {

        BlockingCommandCallback blockingCommandCallback = new BlockingCommandCallback( callback );

        executeAsync( requestBuilder, host, blockingCommandCallback, blockingCommandCallback.getCompletionSemaphore() );

        CommandResult commandResult = blockingCommandCallback.getCommandResult();

        if ( commandResult == null )
        {
            commandResult = new CommandResultImpl( null, null, null, CommandStatus.TIMEOUT );
        }

        return commandResult;
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        executeAsync( requestBuilder, host, callback, null );
    }


    @Override
    public void executeAsync( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {
        executeAsync( requestBuilder, host, null );
    }


    @Override
    public boolean isLocal()
    {
        return false;
    }


    private void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback,
                               Semaphore semaphore ) throws CommandException
    {
        Preconditions.checkNotNull( requestBuilder );
        Preconditions.checkNotNull( host );

        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }

        if ( !( host instanceof ContainerHost ) )
        {
            throw new CommandException( "Operation not allowed" );
        }

        CommandRequest request = new CommandRequest( requestBuilder, host.getId() );
        //cache callback
        commandResponseListener.addCallback( request.getRequestId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command request to remote peer counterpart
        try
        {
            sendRequest( request, RecipientType.COMMAND_REQUEST.name(), Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT );
        }
        catch ( PeerException e )
        {
            throw new CommandException( e );
        }
    }


    @Override
    public Template getTemplate( final String templateName ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.getTemplate( templateName );
    }


    @Override
    public <T, V> V sendRequest( final T request, String recipient, final int timeout, Class<V> responseType )
            throws PeerException
    {
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        //send request
        MessageRequest messageRequest = sendRequestInternal( request, recipient, timeout );

        //wait for response here
        MessageResponse messageResponse = messageResponseListener.waitResponse( messageRequest.getId(), timeout );

        if ( messageResponse != null )
        {
            if ( messageResponse.getException() != null )
            {
                throw new PeerException( messageResponse.getException() );
            }
            else if ( messageResponse.getPayload() != null )
            {
                return messageResponse.getPayload().getMessage( responseType );
            }
        }

        return null;
    }


    @Override
    public <T> void sendRequest( final T request, final String recipient, final int timeout ) throws PeerException
    {
        sendRequestInternal( request, recipient, timeout );
    }


    private <T> MessageRequest sendRequestInternal( final T request, final String recipient, final int timeout )
            throws PeerException
    {
        Preconditions.checkNotNull( request, "Invalid request" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeout > 0, "Timeout must be greater than 0" );

        MessageRequest messageRequest = new MessageRequest( new Payload( request, localPeer.getId() ), recipient );
        Message message = messenger.createMessage( messageRequest );

        try
        {
            messenger.sendMessage( this, message, RecipientType.PEER_REQUEST_LISTENER.name(),
                    Timeouts.PEER_MESSAGE_TIMEOUT );
        }
        catch ( MessageException e )
        {
            throw new PeerException( e );
        }

        return messageRequest;
    }
}
