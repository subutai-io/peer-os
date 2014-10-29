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


    public RemotePeerImpl( final PeerInfo peerInfo, final Messenger messenger )
    {
        //subscribe to command response messages from remote peer
        commandResponseMessageListener = new CommandResponseMessageListener();
        messenger.addMessageListener( commandResponseMessageListener );

        this.peerInfo = peerInfo;
        this.messenger = messenger;
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
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient
                .createContainers( creatorPeerId, environmentId, templates, quantity, strategyId, criteria );
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
        RemotePeerRestClient remotePeerRestClient = new RemotePeerRestClient( 1000000, peerInfo.getIp(), "8181" );
        return remotePeerRestClient.isConnected( host );
    }


    @Override
    public CommandResult execute( final RequestBuilder requestBuilder, final Host host ) throws CommandException
    {

        BlockingCommandCallback callback = new BlockingCommandCallback();

        executeAsync( requestBuilder, host, callback, callback.getCompletionSemaphore() );

        callback.waitCompletion();

        CommandResult commandResult = callback.getCommandResult();

        if ( commandResult == null )
        {
            commandResult = new CommandResult( requestBuilder.getCommandId(), null, null, null, CommandStatus.TIMEOUT );
        }

        return commandResult;
    }


    @Override
    public void execute( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback )
            throws CommandException
    {
        executeAsync( requestBuilder, host, callback, null );
    }


    private void executeAsync( final RequestBuilder requestBuilder, final Host host, final CommandCallback callback,
                               Semaphore semaphore ) throws CommandException
    {
        if ( !host.isConnected() )
        {
            throw new CommandException( "Host disconnected." );
        }
        //cache callback
        commandResponseMessageListener
                .addCallback( requestBuilder.getCommandId(), callback, requestBuilder.getTimeout(), semaphore );

        //send command message to remote peer
        try
        {
            Message message = messenger.createMessage( new CommandRequest( requestBuilder, host ) );
            messenger.sendMessage( this, message, CommandRecipientType.COMMAND_REQUEST.name(),
                    requestBuilder.getTimeout() );
        }
        catch ( MessageException e )
        {
            throw new CommandException( e );
        }
    }
}
