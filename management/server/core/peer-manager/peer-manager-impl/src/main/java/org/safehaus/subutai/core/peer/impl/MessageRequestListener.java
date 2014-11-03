package org.safehaus.subutai.core.peer.impl;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;


public class MessageRequestListener extends MessageListener
{

    private PeerManager peerManager;
    private Messenger messenger;
    private Set<RequestListener> listeners;
    private ExecutorService notifier = Executors.newCachedThreadPool();


    protected MessageRequestListener( PeerManager peerManager, Messenger messenger, Set<RequestListener> listeners )
    {
        super( RecipientType.PEER_REQUEST_LISTENER.name() );
        this.peerManager = peerManager;
        this.messenger = messenger;
        this.listeners = listeners;
    }


    @Override
    public void onMessage( final Message message )
    {
        MessageRequest messageRequest = message.getPayload( MessageRequest.class );

        for ( RequestListener listener : listeners )
        {
            if ( messageRequest.getRecipient().equalsIgnoreCase( listener.getRecipient() ) )
            {
                //notify relevant recipient
                notifier.execute( new RequestNotifier( peerManager, messenger, listener, message, messageRequest ) );

                return;
            }
        }
    }
}
