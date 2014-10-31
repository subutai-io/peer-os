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


/**
 * Created by timur on 10/22/14.
 */
public class RemotePeerImpl implements RemotePeer
{
    private static final Logger LOG = LoggerFactory.getLogger( RemotePeerImpl.class.getName() );

    protected PeerInfo peerInfo;
    protected Messenger messenger;

    private CommandResponseMessageListener commandResponseMessageListener;
    private CreateContainerResponseListener createContainerResponseListener;


    public RemotePeerImpl( final PeerInfo peerInfo, final Messenger messenger,
                           CommandResponseMessageListener commandResponseMessageListener,
                           CreateContainerResponseListener createContainerResponseListener )
    {
        this.peerInfo = peerInfo;
        this.messenger = messenger;
        this.commandResponseMessageListener = commandResponseMessageListener;
    }


    @Override
    public boolean isOnline() throws PeerException
    {
        return false;
    }


    @Override
    public UUID getId()
    {
        return peerInfo.getId();
    }


    @Override
    public String getName()
    {
        return peerInfo.getName();
    }


    @Override
    public UUID getOwnerId()
    {
        return null;
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
        //        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(),
        // "8181" );
        //        return remotePeerRestClient
        //                .createContainers( creatorPeerId, environmentId, templates, quantity, strategyId, criteria );
        try
        {
            //send create request
            CreateContainerRequest request =
                    new CreateContainerRequest( creatorPeerId, environmentId, templates, quantity, strategyId,
                            criteria );
            Message createContainerMessage = messenger.createMessage( request );
            messenger.sendMessage( this, createContainerMessage, RecipientType.CONTAINER_CREATE_REQUEST.name(),
                    Constants.CREATE_CONTAINER_REQUEST_TIMEOUT );

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
    public boolean startContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.startContainer( containerHost );
    }


    @Override
    public boolean stopContainer( final ContainerHost containerHost ) throws PeerException
    {
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( peerInfo.getIp(), "8181" );
        return remotePeerRestClient.stopContainer( containerHost );
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
            commandResult = new CommandResult( requestBuilder.getCommandId(), null, null, null, CommandStatus.TIMEOUT );
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
        //cache callback
        commandResponseMessageListener
                .addCallback( requestBuilder.getCommandId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command message to remote peer
        try
        {
            Message message = messenger.createMessage( new CommandRequest( requestBuilder, ( ContainerHost ) host ) );
            messenger.sendMessage( this, message, RecipientType.COMMAND_REQUEST.name(),
                    Constants.COMMAND_REQUEST_MESSAGE_TIMEOUT );
        }
        catch ( MessageException e )
        {
            throw new CommandException( e );
        }
    }
}
