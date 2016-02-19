package io.subutai.core.channel.impl.interceptor;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.cxf.message.Message;

import io.subutai.common.settings.PeerSettings;
import io.subutai.core.channel.impl.ChannelManagerImpl;
import io.subutai.core.peer.api.PeerManager;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ServerInInterceptorTest
{
    private ServerInInterceptor cxfInterceptor;


    @Mock
    ChannelManagerImpl channelManager;
    @Mock
    PeerManager peerManager;
    @Mock
    Message message;
    @Mock
    Object object;
    @Mock
    PeerSettings peerSettings;


    @Before
    public void setUp() throws Exception
    {
        cxfInterceptor = new ServerInInterceptor( channelManager, peerManager );
    }


    @Test
    public void testHandleMessageException() throws Exception
    {
//        cxfInterceptor.handleMessage( message );
    }


    @Test
    public void testHandleMessage() throws Exception
    {
        when( message.get( any( Message.class ) ) ).thenReturn( "http://example.com/pages/" );

        //cxfInterceptor.handleMessage( message );
    }
}