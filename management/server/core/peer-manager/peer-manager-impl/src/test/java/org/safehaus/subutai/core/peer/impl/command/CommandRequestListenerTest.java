package org.safehaus.subutai.core.peer.impl.command;


import java.io.PrintStream;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.command.CommandCallback;
import org.safehaus.subutai.common.command.CommandException;
import org.safehaus.subutai.common.command.CommandResult;
import org.safehaus.subutai.common.command.RequestBuilder;
import org.safehaus.subutai.common.command.Response;
import org.safehaus.subutai.common.peer.Host;
import org.safehaus.subutai.common.peer.Peer;
import org.safehaus.subutai.common.peer.PeerException;
import org.safehaus.subutai.core.peer.api.LocalPeer;
import org.safehaus.subutai.core.peer.api.Payload;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.safehaus.subutai.core.peer.impl.RecipientType;
import org.safehaus.subutai.core.peer.impl.Timeouts;

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
        listener = new CommandRequestListener( localPeer, peerManager );
        when( payload.getMessage( CommandRequest.class ) ).thenReturn( commandRequest );
        when( peerManager.getPeer( any( UUID.class ) ) ).thenReturn( sourcePeer );
        when( localPeer.bindHost( any( UUID.class ) ) ).thenReturn( host );
    }


    @Test
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

        verify( peerManager, times( 2 ) ).getPeer( any( UUID.class ) );
    }


    @Test
    public void testCommandRequestCallback() throws Exception
    {
        CommandRequestListener.CommandRequestCallback callback =
                new CommandRequestListener.CommandRequestCallback( commandRequest, sourcePeer );
        when( commandRequest.getEnvironmentId() ).thenReturn( UUID.randomUUID() );

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
