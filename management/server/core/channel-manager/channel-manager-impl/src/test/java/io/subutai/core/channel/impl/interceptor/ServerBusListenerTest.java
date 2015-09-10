package io.subutai.core.channel.impl.interceptor;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.channel.impl.ChannelManagerImpl;

import org.slf4j.Logger;

import org.apache.cxf.Bus;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class ServerBusListenerTest
{
    private ServerBusListener cxfBusListener;
    @Mock
    ChannelManagerImpl channelManager;
    @Mock
    Bus bus;
    @Mock
    Logger logger;

    @Before
    public void setUp() throws Exception
    {
        cxfBusListener = new ServerBusListener();
        cxfBusListener.setChannelManager( channelManager );
    }


    @Test
    public void testBusRegistered() throws Exception
    {
        cxfBusListener.busRegistered( bus );
    }


    @Test
    public void testGetChannelManagerImpl() throws Exception
    {
        assertNotNull(cxfBusListener.getChannelManagerImpl());
    }


}