package org.safehaus.subutai.core.peer.impl;


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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandRequestMessageListener extends MessageListener
{
    private static final Logger LOG = LoggerFactory.getLogger( CommandRequestMessageListener.class.getName() );

    private LocalPeer localPeer;
    private Messenger messenger;
    private PeerManager peerManager;


    protected CommandRequestMessageListener( final LocalPeer localPeer, final Messenger messenger,
                                             final PeerManager peerManager )
    {
        super( CommandRecipientType.COMMAND_REQUEST.name() );
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
            localPeer.execute( commandRequest.getRequestBuilder(), commandRequest.getHost(), new CommandCallback()
            {
                @Override
                public void onResponse( final Response response, final CommandResult commandResult )
                {
                    try
                    {
                        Message commandResponseMsg =
                                messenger.createMessage( new CommandResponse( response, commandResult ) );
                        Peer sourcePeer = peerManager.getPeer( message.getSourcePeerId() );
                        long timeLeft =
                                commandRequest.getRequestBuilder().getTimeout() * 1000 - ( System.currentTimeMillis()
                                        - commandRequest.getCreateDate().getTime() );
                        //send response only if timeout is still not expired
                        if ( timeLeft > 0 )
                        {
                            messenger.sendMessage( sourcePeer, commandResponseMsg,
                                    CommandRecipientType.COMMAND_RESPONSE.name(), ( int ) ( timeLeft / 1000 ) );
                        }
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
