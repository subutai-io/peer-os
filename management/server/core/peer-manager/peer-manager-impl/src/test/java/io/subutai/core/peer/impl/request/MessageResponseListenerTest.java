package io.subutai.core.peer.impl.request;


import java.io.PrintStream;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.peer.PeerException;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.MessageStatus;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.peer.impl.request.MessageRequest;
import io.subutai.core.peer.impl.request.MessageResponse;
import io.subutai.core.peer.impl.request.MessageResponseListener;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class MessageResponseListenerTest
{
    private static final int REQUEST_TIMEOUT = 3;
    private static final int RESPONSE_TIMEOUT = 3;
    @Mock
    Messenger messenger;
    @Mock
    Map<UUID, Semaphore> semaphoreMap;
    @Mock
    ExpiringCache<UUID, MessageResponse> responses;
    @Mock
    Message message;
    @Mock
    MessageResponse messageResponse;
    @Mock
    Semaphore semaphore;
    @Mock
    MessageRequest messageRequest;
    @Mock
    InterruptedException interruptedException;


    MessageResponseListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new MessageResponseListener( messenger );
        listener.semaphoreMap = semaphoreMap;
        listener.responses = responses;
        when( message.getPayload( MessageResponse.class ) ).thenReturn( messageResponse );
        when( semaphoreMap.remove( any( UUID.class ) ) ).thenReturn( semaphore );
        when( messenger.getMessageStatus( any( UUID.class ) ) ).thenReturn( MessageStatus.IN_PROCESS )
                                                               .thenReturn( MessageStatus.SENT );
        when( messageRequest.getId() ).thenReturn( UUID.randomUUID() );
        when( semaphoreMap.get( any( UUID.class ) ) ).thenReturn( semaphore );
    }


    @Test
    public void testDispose() throws Exception
    {
        listener.dispose();

        verify( responses ).dispose();
    }


    @Test
    public void testOnMessage() throws Exception
    {
        listener.onMessage( message );

        verify( responses ).put( any( UUID.class ), eq( messageResponse ), anyLong() );
        verify( semaphoreMap ).remove( any( UUID.class ) );
        verify( semaphore ).release();
    }


    @Test
    public void testWaitResponse() throws Exception
    {

        listener.waitResponse( messageRequest, REQUEST_TIMEOUT, RESPONSE_TIMEOUT );

        verify( responses ).remove( any( UUID.class ) );

        when( messenger.getMessageStatus( any( UUID.class ) ) ).thenReturn( MessageStatus.EXPIRED );

        try
        {
            listener.waitResponse( messageRequest, REQUEST_TIMEOUT, RESPONSE_TIMEOUT );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }

        when( messenger.getMessageStatus( any( UUID.class ) ) ).thenReturn( MessageStatus.SENT );

        doThrow( new MessageException( "" ) ).when( messenger ).getMessageStatus( any( UUID.class ) );

        try
        {
            listener.waitResponse( messageRequest, REQUEST_TIMEOUT, RESPONSE_TIMEOUT );
            fail( "Expected PeerException" );
        }
        catch ( PeerException e )
        {
        }


        reset( messenger );

        when( messenger.getMessageStatus( any( UUID.class ) ) ).thenReturn( MessageStatus.SENT );

        doThrow( interruptedException ).when( semaphore ).tryAcquire( anyLong(), any( TimeUnit.class ) );

        listener.waitResponse( messageRequest, REQUEST_TIMEOUT, RESPONSE_TIMEOUT );

        verify( interruptedException ).printStackTrace( any( PrintStream.class ) );
    }
}
