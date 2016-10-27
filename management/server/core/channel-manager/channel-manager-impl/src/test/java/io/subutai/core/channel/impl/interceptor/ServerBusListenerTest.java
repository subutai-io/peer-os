package io.subutai.core.channel.impl.interceptor;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.Bus;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.message.Message;

import io.subutai.core.identity.api.IdentityManager;
import io.subutai.core.peer.api.PeerManager;
import io.subutai.core.security.api.SecurityManager;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class ServerBusListenerTest
{
    @Mock
    Bus bus;
    @Mock
    List<Interceptor<? extends Message>> inInterceptors;
    @Mock
    List<Interceptor<? extends Message>> outInterceptors;
    @Mock
    SecurityManager securityManager;
    @Mock
    IdentityManager identityManager;
    @Mock
    PeerManager peerManager;

    ServerBusListener serverBusListener;


    @Before
    public void setUp() throws Exception
    {
        serverBusListener = new ServerBusListener( securityManager, identityManager, peerManager );
        doReturn( inInterceptors ).when( bus ).getInInterceptors();
        doReturn( outInterceptors ).when( bus ).getOutInterceptors();
    }


    @Test
    public void testBusRegistered() throws Exception
    {
        serverBusListener.busRegistered( bus );

        verify( inInterceptors ).add( isA( AccessControlInterceptor.class ) );
        verify( outInterceptors ).add( isA( ClientOutInterceptor.class ) );
    }
}
