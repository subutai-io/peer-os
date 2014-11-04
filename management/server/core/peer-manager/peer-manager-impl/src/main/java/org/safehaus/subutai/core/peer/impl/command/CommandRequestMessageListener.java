package org.safehaus.subutai.core.peer.impl.command;


import org.safehaus.subutai.common.exception.CommandException;
import org.safehaus.subutai.common.protocol.CommandCallback;
import org.safehaus.subutai.common.protocol.CommandResult;
import org.safehaus.subutai.common.protocol.Response;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.safehaus.subutai.core.peer.impl.Timeouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandRequestMessageListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandRequestMessageListener.class.getName() );

    private LocalPeer localPeer;
    private Messenger messenger;
    private PeerManager peerManager;


    public CommandRequestMessageListener( final LocalPeer localPeer, final Messenger messenger,
                                          final PeerManager peerManager )
    {
        super( RecipientType.COMMAND_REQUEST.name() );
        this.localPeer = localPeer;
        this.messenger = messenger;
        this.peerManager = peerManager;
    }


    @Override
    public void onMessage( final Message message )
    {
        final CommandRequest commandRequest = message.getPayload( CommandRequest.class );

        try
        {
            localPeer.executeAsync( commandRequest.getRequestBuilder(), commandRequest.getHost(), new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    try
                    {
                        Message commandResponseMsg = messenger.createMessage(
                                new CommandResponse( commandRequest.getRequestId(), response, commandResult ) );
                        Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );

                        messenger.sendMessage( sourcePeer, commandResponseMsg, RecipientType.COMMAND_RESPONSE.name(),
                                Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT );
                    }
                    catch ( MessageException e )
                    {
                        LOG.error( "Error in onMessage", e );
                    }
                }
            } );
        }
        catch ( CommandException e )
        {
            LOG.error( "Error in onMessage", e );
        }
    }
}
