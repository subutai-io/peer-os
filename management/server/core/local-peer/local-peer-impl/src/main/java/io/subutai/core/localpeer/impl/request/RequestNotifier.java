package io.subutai.core.localpeer.impl.request;


import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.MessageRequest;
import io.subutai.common.peer.MessageResponse;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.peer.Timeouts;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.PeerManager;


public class RequestNotifier implements Runnable
{
    private static final Logger LOG = LoggerFactory.getLogger( RequestNotifier.class.getName() );


    private Messenger messenger;
    private RequestListener listener;
    private Message message;
    private MessageRequest messageRequest;
    private LocalPeer localPeer;


    public RequestNotifier( final Messenger messenger, final RequestListener listener, final Message message,
                            final MessageRequest messageRequest, final LocalPeer localPeer )
    {

        this.messenger = messenger;
        this.listener = listener;
        this.message = message;
        this.messageRequest = messageRequest;
        this.localPeer = localPeer;
    }


    protected PeerManager getPeerManager() throws MessageException
    {
        try
        {
            return ServiceLocator.getServiceNoCache( PeerManager.class );
        }
        catch ( NamingException e )
        {
            throw new MessageException( e );
        }
    }


    @Override
    public void run()
    {
        //notify listener and obtain response
        MessageResponse messageResponse = null;
        try
        {
            Object response = listener.onRequest( messageRequest.getPayload() );
            if ( response != null )
            {
                messageResponse =
                        new MessageResponse( messageRequest.getId(), new Payload( response, localPeer.getId() ), null );
            }
        }
        catch ( Exception e )
        {
            LOG.error( "Error thrown by RequestListener", e );
            messageResponse = new MessageResponse( messageRequest.getId(), null, e.getMessage() );
        }

        //send message if not null
        if ( messageResponse != null )
        {
            //send response back
            Message responseMessage = messenger.createMessage( messageResponse );
            try
            {
                Peer sourcePeer = getPeerManager().getPeer( message.getSourcePeerId() );
                messenger.sendMessage( sourcePeer, responseMessage, RecipientType.PEER_RESPONSE_LISTENER.name(),
                        Timeouts.PEER_MESSAGE_TIMEOUT, messageRequest.getHeaders() );
            }
            catch ( MessageException e )
            {
                LOG.error( "Error sending response to peer message", e );
            }
        }
    }
}
