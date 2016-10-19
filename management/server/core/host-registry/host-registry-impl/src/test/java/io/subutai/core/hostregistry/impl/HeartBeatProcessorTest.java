package io.subutai.core.hostregistry.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.host.HeartBeat;
import io.subutai.common.host.ResourceHostInfo;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class HeartBeatProcessorTest
{
    @Mock
    HostRegistryImpl hostRegistry;
    @Mock
    HeartBeat heartBeat;

    HeartbeatProcessor heartbeatProcessor;


    @Before
    public void setUp() throws Exception
    {
        heartbeatProcessor = new HeartbeatProcessor( hostRegistry );
    }


    @Test
    public void onHeartbeat() throws Exception
    {

        heartbeatProcessor.onHeartbeat( heartBeat );

        verify( hostRegistry ).registerHost( any( ResourceHostInfo.class ), anySet() );
        verify( heartBeat ).getHostInfo();
        verify( heartBeat ).getAlerts();
    }
}
