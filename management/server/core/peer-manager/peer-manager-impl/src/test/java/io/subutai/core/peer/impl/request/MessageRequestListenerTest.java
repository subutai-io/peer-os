package io.subutai.core.peer.impl.request;


import java.util.concurrent.ExecutorService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.LocalPeer;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.peer.api.RequestListener;
import io.subutai.core.peer.impl.request.MessageRequest;
import io.subutai.core.peer.impl.request.MessageRequestListener;
import io.subutai.core.peer.impl.request.RequestNotifier;

import com.google.common.collect.Sets;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageRequestListenerTest
{
    private static final String RECIPIENT = "recipient";
    @Mock
    PeerManager peerManager;
    @Mock
    Messenger messenger;
    @Mock
    RequestListener requestListener;
    @Mock
    ExecutorService notifier;
    @Mock
    Message message;
    @Mock
    MessageRequest messageRequest;
    @Mock
    LocalPeer localPeer;

    MessageRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new MessageRequestListener( peerManager, messenger );
        listener.notifier = notifier;
        when( requestListener.getRecipient() ).thenReturn( RECIPIENT );
        when( messageRequest.getRecipient() ).thenReturn( RECIPIENT );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
        when( localPeer.getRequestListeners() ).thenReturn( Sets.newHashSet( requestListener ) );
        when( message.getPayload( MessageRequest.class ) ).thenReturn( messageRequest );
    }


    @Test
    public void testDispose() throws Exception
    {
        listener.dispose();

        verify( notifier ).shutdown();
    }


    @Test
    public void testOnMessage() throws Exception
    {

        listener.onMessage( message );

        verify( notifier ).execute( isA( RequestNotifier.class ) );
    }
}
