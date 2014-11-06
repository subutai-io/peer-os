package org.safehaus.subutai.core.metric.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.metric.api.MonitorException;
import org.safehaus.subutai.core.peer.api.Payload;
import org.slf4j.Logger;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RemoteAlertListenerTest
{

    @Mock
    MonitorImpl monitor;
    @Mock
    Payload payload;
    @Mock
    ContainerHostMetricImpl metric;
    @Mock
    Logger logger;

    RemoteAlertListener remoteAlertListener;


    @Before
    public void setUp() throws Exception
    {
        remoteAlertListener = new RemoteAlertListener( monitor );
        remoteAlertListener.LOG = logger;
    }


    @Test
    public void testOnMessage() throws Exception
    {
        when( payload.getMessage( ContainerHostMetricImpl.class ) ).thenReturn( metric );

        remoteAlertListener.onRequest( payload );

        verify( monitor ).alertThresholdExcess( metric );

        doThrow( new MonitorException( "" ) ).when( monitor ).alertThresholdExcess( metric );

        remoteAlertListener.onRequest( payload );

        verify( logger ).error( anyString(), isA( MonitorException.class ) );
    }
}
