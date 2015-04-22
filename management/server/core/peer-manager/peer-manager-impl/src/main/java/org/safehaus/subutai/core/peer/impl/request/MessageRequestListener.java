package org.safehaus.subutai.core.peer.impl.request;


import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.safehaus.subutai.common.protocol.Disposable;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.api.RequestListener;
import org.safehaus.subutai.core.peer.impl.RecipientType;


public class MessageRequestListener extends MessageListener implements Disposable
{

    private PeerManager peerManager;
    private Messenger messenger;
    private Set<RequestListener> listeners;
    protected ExecutorService notifier = Executors.newCachedThreadPool();


    public MessageRequestListener( PeerManager peerManager, Messenger messenger, Set<RequestListener> listeners )
    {
        super( RecipientType.PEER_REQUEST_LISTENER.name() );
        this.peerManager = peerManager;
        this.messenger = messenger;
        this.listeners = listeners;
    }


    public void dispose()
    {
        notifier.shutdown();
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
