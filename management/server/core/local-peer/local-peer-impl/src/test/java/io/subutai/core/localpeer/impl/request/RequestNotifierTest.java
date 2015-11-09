package io.subutai.core.localpeer.impl.request;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.MessageRequest;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.RequestListener;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RequestNotifierTest
{
    @Mock
    PeerManager peerManager;
    @Mock
    Messenger messenger;
    @Mock
    RequestListener requestListener;
    @Mock
    Message message;
    @Mock
    MessageRequest messageRequest;
    @Mock
    Payload payload;
    @Mock
    Peer peer;
    @Mock
    LocalPeer localPeer;
    @Mock
    MessageException messageException;
    @Mock
    RuntimeException exception;
    Object response = new Object();

    RequestNotifier requestNotifier;


    @Before
    public void setUp() throws Exception
    {
        requestNotifier = new RequestNotifier( messenger, requestListener, message, messageRequest, localPeer );
        when( messageRequest.getPayload() ).thenReturn( payload );
        when( requestListener.onRequest( payload ) ).thenReturn( response );
        when( peerManager.getPeer( any( String.class ) ) ).thenReturn( peer );
        when( peerManager.getLocalPeer() ).thenReturn( localPeer );
    }


    @Test
    public void testRun() throws Exception
    {
        requestNotifier.run();

        verify( messenger ).sendMessage( eq( peer ), any( Message.class ), anyString(), anyInt(), anyMap() );

        doThrow( messageException ).when( messenger )
                                   .sendMessage( eq( peer ), any( Message.class ), anyString(), anyInt(), anyMap() );

        requestNotifier.run();

        verify( messageException ).printStackTrace( any( PrintStream.class ) );

        doThrow( exception ).when( requestListener ).onRequest( payload );

        requestNotifier.run();

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
