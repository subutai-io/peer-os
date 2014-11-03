package org.safehaus.subutai.core.peer.impl;


import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.CommandStatus;
import org.safehaus.subutai.common.protocol.RequestBuilder;
import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.lxc.quota.api.QuotaEnum;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.ContainerHost;
import org.safehaus.subutai.core.peer.api.Host;
import org.safehaus.subutai.core.peer.api.PeerException;
import org.safehaus.subutai.core.peer.api.PeerInfo;
import org.safehaus.subutai.core.peer.api.RemotePeer;
import org.safehaus.subutai.core.strategy.api.Criteria;
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

    protected PeerInfo peerInfo;
    protected Messenger messenger;

    private CommandResponseMessageListener commandResponseMessageListener;
    private CreateContainerResponseListener createContainerResponseListener;
    private MessageResponseListener messageResponseListener;


    public RemotePeerImpl( final PeerInfo peerInfo, final Messenger messenger,
                           CommandResponseMessageListener commandResponseMessageListener,
                           CreateContainerResponseListener createContainerResponseListener,
                           MessageResponseListener messageResponseListener )
    {
        this.peerInfo = peerInfo;
        this.messenger = messenger;
        this.commandResponseMessageListener = commandResponseMessageListener;
        this.createContainerResponseListener = createContainerResponseListener;
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
                                                final String strategyId, final List<Criteria> criteria )
            throws ContainerCreateException
    {
        try
        {
            //send create request
            CreateContainerRequest request =
                    new CreateContainerRequest( creatorPeerId, environmentId, templates, quantity, strategyId,
                            criteria );
            Message createContainerMessage = messenger.createMessage( request );
            messenger.sendMessage( this, createContainerMessage, RecipientType.CONTAINER_CREATE_REQUEST.name(),
                    Timeouts.CREATE_CONTAINER_REQUEST_TIMEOUT );

            //wait for response
            return createContainerResponseListener.waitContainers( request.getRequestId() );
        }
        catch ( MessageException e )
        {
            LOG.error( "Error in createContainers", e );
            throw new ContainerCreateException( e.getMessage() );
        }
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
        //        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 10000, peerInfo.getIp(),
        // "8181" );
        //        return remotePeerRestClient.getQuota( host, quota );
    }


    @Override
    public void setQuota( final ContainerHost host, final QuotaEnum quota, final String value ) throws PeerException
    {
        throw new PeerException( "Operation not allowed." );
        //        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 10000, peerInfo.getIp(),
        // "8181" );
        //        remotePeerRestClient.setQuota( host, quota, value );
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

        blockingCommandCallback.waitCompletion();

        CommandResult commandResult = blockingCommandCallback.getCommandResult();

        if ( commandResult == null )
        {
            commandResult = new CommandResult( null, null, null, CommandStatus.TIMEOUT );
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
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }

        if ( !( host instanceof ContainerHost ) )
        {
            throw new CommandException( "Operation not allowed" );
        }

        CommandRequest request = new CommandRequest( requestBuilder, ( ContainerHost ) host );
        //cache callback
        commandResponseMessageListener
                .addCallback( request.getRequestId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command message to remote peer
        try
        {
            Message message = messenger.createMessage( request );
            messenger.sendMessage( this, message, RecipientType.COMMAND_REQUEST.name(),
                    Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT );
        }
        catch ( MessageException e )
        {
            throw new CommandException( e );
        }
    }


    @Override
    public Template getTemplate( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.getTemplate( containerHost );
    }


    @Override
    public <T, V> V sendRequest( final T payload, String recipient, final int timeout, Class<V> responseType )
            throws PeerException
    {
        Preconditions.checkNotNull( payload, "Invalid payload" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeout > 0, "Timeout must be greater than 0" );
        Preconditions.checkNotNull( responseType, "Invalid response type" );

        MessageRequest messageRequest = new MessageRequest<>( payload, recipient );
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
                return responseType.cast( messageResponse.getPayload() );
            }
        }

        return null;
    }
}
