package io.subutai.core.peer.impl.command;


import java.util.UUID;
import java.util.concurrent.Semaphore;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.cache.EntryExpiryCallback;
import org.safehaus.subutai.common.cache.ExpiringCache;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.Response;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.impl.Timeouts;
import io.subutai.core.peer.impl.command.CommandResponse;
import io.subutai.core.peer.impl.command.CommandResponseListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandResponseListenerTest
{
    private static final UUID COMMAND_ID = UUID.randomUUID();
    private static final long TIMEOUT = 100;
    @Mock
    ExpiringCache<UUID, CommandCallback> callbacks;
    @Mock
    CommandCallback callback;
    @Mock
    Semaphore semaphore;
    @Mock
    Payload payload;
    @Mock
    CommandResponse commandResponse;


    CommandResponseListener commandResponseListener;


    @Before
    public void setUp() throws Exception
    {
        commandResponseListener = new CommandResponseListener();
        commandResponseListener.callbacks = callbacks;
        when( callbacks.get( COMMAND_ID ) ).thenReturn( callback );
        when( payload.getMessage( CommandResponse.class ) ).thenReturn( commandResponse );
        when( commandResponse.getRequestId() ).thenReturn( COMMAND_ID );
    }


    @Test
    public void testDispose() throws Exception
    {
        commandResponseListener.dispose();

        verify( callbacks ).dispose();
    }


    @Test
    public void testAddCallback() throws Exception
    {
        commandResponseListener.addCallback( COMMAND_ID, callback, ( int ) TIMEOUT, semaphore );

        verify( callbacks ).put( eq( COMMAND_ID ), eq( callback ),
                eq( TIMEOUT * 1000 + Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT * 2 * 1000 ),
                isA( EntryExpiryCallback.class ) );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        commandResponseListener.onRequest( payload );

        verify( callback ).onResponse( any( Response.class ), any( CommandResult.class ) );

        when( payload.getMessage( CommandResponse.class ) ).thenReturn( null );

        commandResponseListener.onRequest( payload );

        verify( callback ).onResponse( any( Response.class ), any( CommandResult.class ) );
    }


    @Test
    public void testCommandResponseExpiryCallback() throws Exception
    {
        CommandResponseListener.CommandResponseExpiryCallback expiryCallback =
                new CommandResponseListener.CommandResponseExpiryCallback( semaphore );

        expiryCallback.onEntryExpiry( null );

        verify( semaphore ).release();
    }
}
