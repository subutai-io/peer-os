package io.subutai.core.metric.impl;


import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.metric.api.MonitorException;

import io.subutai.common.peer.Payload;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class RemoteAlertListenerTest
{

    @Mock
    MonitorImpl monitor;
    @Mock
    Payload payload;
    @Mock
    ContainerHostMetricImpl metric;


    RemoteAlertListener remoteAlertListener;


    @Before
    public void setUp() throws Exception
    {
        remoteAlertListener = new RemoteAlertListener( monitor );
    }


    @Test
    public void testOnMessage() throws Exception
    {
        when( payload.getMessage( ContainerHostMetricImpl.class ) ).thenReturn( metric );

        remoteAlertListener.onRequest( payload );

        verify( monitor ).notifyOnAlert( metric );

        MonitorException exception = mock( MonitorException.class );

        doThrow( exception ).when( monitor ).notifyOnAlert( metric );

        remoteAlertListener.onRequest( payload );

        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
