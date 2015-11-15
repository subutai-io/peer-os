package io.subutai.core.localpeer.impl.command;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.command.CommandCallback;
import io.subutai.common.command.CommandException;
import io.subutai.common.command.CommandRequest;
import io.subutai.common.command.CommandResponse;
import io.subutai.common.command.CommandResult;
import io.subutai.common.command.RequestBuilder;
import io.subutai.common.command.Response;
import io.subutai.common.peer.Host;
import io.subutai.common.peer.LocalPeer;
import io.subutai.common.peer.Payload;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.RecipientType;
import io.subutai.common.peer.Timeouts;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class CommandRequestListenerTest
{
    @Mock
    LocalPeer localPeer;
    @Mock
    PeerManager peerManager;
    @Mock
    Payload payload;
    @Mock
    Peer sourcePeer;
    @Mock
    Host host;
    @Mock
    CommandRequest commandRequest;
    @Mock
    CommandException exception;
    @Mock
    Response response;
    @Mock
    CommandResult commandResult;
    @Mock
    PeerException peerException;


    CommandRequestListener listener;


    @Before
    public void setUp() throws Exception
    {
        listener = new CommandRequestListener();
        when( payload.getMessage( CommandRequest.class ) ).thenReturn( commandRequest );
        when( peerManager.getPeer( any( String.class ) ) ).thenReturn( sourcePeer );
        when( localPeer.bindHost( any( String.class ) ) ).thenReturn( host );
    }


    @Test
    @Ignore
    public void testOnRequest() throws Exception
    {
        listener.onRequest( payload );

        verify( localPeer ).executeAsync( any( RequestBuilder.class ), eq( host ), any( CommandCallback.class ) );

        doThrow( exception ).when( localPeer )
                            .executeAsync( any( RequestBuilder.class ), eq( host ), any( CommandCallback.class ) );

        listener.onRequest( payload );

        verify( exception ).printStackTrace( any( PrintStream.class ) );

        when( payload.getMessage( CommandRequest.class ) ).thenReturn( null );

        listener.onRequest( payload );

        verify( peerManager, times( 2 ) ).getPeer( any( String.class ) );
    }


    @Test
    public void testCommandRequestCallback() throws Exception
    {
        CommandRequestListener.CommandRequestCallback callback =
                new CommandRequestListener.CommandRequestCallback( commandRequest, sourcePeer );
        when( commandRequest.getEnvironmentId() ).thenReturn( UUID.randomUUID().toString() );

        callback.onResponse( response, commandResult );

        verify( sourcePeer ).sendRequest( any( CommandResponse.class ), eq( RecipientType.COMMAND_RESPONSE.name() ),
                eq( Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT ), anyMap() );

        doThrow( peerException ).when( sourcePeer )
                                .sendRequest( any( CommandResponse.class ), eq( RecipientType.COMMAND_RESPONSE.name() ),
                                        eq( Timeouts.COMMAND_REQUEST_MESSAGE_TIMEOUT ), anyMap() );

        callback.onResponse( response, commandResult );


        verify( peerException ).printStackTrace( any( PrintStream.class ) );
    }
}
