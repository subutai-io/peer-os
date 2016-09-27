package io.subutai.core.channel.impl.interceptor;


import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.message.Message;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ClientHeaderInterceptorTest
{

    @Mock
    PeerManager peerManager;
    @Mock
    LocalPeer localPeer;
    @Mock
    Map<String, List> headers;


    ClientHeaderInterceptor interceptor;


    @Before
    public void setUp() throws Exception
    {
        interceptor = new ClientHeaderInterceptor( peerManager );

        doReturn( localPeer ).when( peerManager ).getLocalPeer();
    }


    @Test
    public void testHandleMessage() throws Exception
    {
        Message message = InterceptorStateHelper.getMessage( InterceptorState.CLIENT_OUT );
        doReturn( headers ).when( message ).get( Message.PROTOCOL_HEADERS );

        interceptor.handleMessage( message );

        verify( headers ).put( eq( Common.SUBUTAI_HTTP_HEADER ), anyList() );
    }
}
