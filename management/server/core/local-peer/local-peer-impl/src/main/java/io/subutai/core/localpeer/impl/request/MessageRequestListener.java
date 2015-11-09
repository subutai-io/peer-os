package io.subutai.core.localpeer.impl.request;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.MessageRequest;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.RequestListener;
import io.subutai.common.protocol.Disposable;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageListener;
import io.subutai.core.messenger.api.Messenger;


public class MessageRequestListener extends MessageListener implements Disposable
{

    private LocalPeer localPeer;
    private Messenger messenger;
    protected ExecutorService notifier = Executors.newCachedThreadPool();


    public MessageRequestListener( LocalPeer localPeer, Messenger messenger )
    {
        super( RecipientType.PEER_REQUEST_LISTENER.name() );
        this.localPeer = localPeer;
        this.messenger = messenger;
    }


    public void dispose()
    {
        notifier.shutdown();
    }


    @Override
    public void onMessage( final Message message )
    {
        MessageRequest messageRequest = message.getPayload( MessageRequest.class );

        for ( RequestListener listener : localPeer.getRequestListeners() )
        {
            if ( messageRequest.getRecipient().equalsIgnoreCase( listener.getRecipient() ) )
            {
                //notify relevant recipient
                notifier.execute( new RequestNotifier( messenger, listener, message, messageRequest, localPeer ) );

                return;
            }
        }
    }
}
