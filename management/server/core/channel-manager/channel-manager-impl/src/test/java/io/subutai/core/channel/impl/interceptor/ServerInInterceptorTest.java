package io.subutai.core.channel.impl.interceptor;


import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.AbstractHTTPDestination;

import io.subutai.common.peer.LocalPeer;
import io.subutai.common.settings.Common;
import io.subutai.core.channel.impl.util.InterceptorState;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ServerInInterceptorTest
{

    @Mock
    SecurityManager securityManager;
    @Mock
    PeerManager peerManager;
    @Mock
    HttpServletRequest request;
    @Mock
    LocalPeer localPeer;

    ServerInInterceptor interceptor;


    @Before
    public void setUp() throws Exception
    {
        interceptor = spy( new ServerInInterceptor( securityManager, peerManager ) );
        doReturn( localPeer ).when( peerManager ).getLocalPeer();
    }


    @Test
    public void testHandleMessage() throws Exception
    {
        Message message = InterceptorStateHelper.getMessage( InterceptorState.SERVER_IN );
        Exchange exchange = message.getExchange();
        doReturn( message ).when( exchange ).getInMessage();
        doReturn( request ).when( message ).get( AbstractHTTPDestination.HTTP_REQUEST );
        doReturn( Common.DEFAULT_PUBLIC_SECURE_PORT ).when( request ).getLocalPort();
        doReturn( "/rest/v1/peer" ).when( request ).getRequestURI();

        interceptor.handleMessage( message );

        verify( interceptor ).handlePeerMessage( anyString(), eq( message ) );

        doReturn( "/rest/v1/env/123/" ).when( request ).getRequestURI();

        interceptor.handleMessage( message );

        verify( interceptor ).handleEnvironmentMessage( anyString(), anyString(), eq( message ) );
    }
}
