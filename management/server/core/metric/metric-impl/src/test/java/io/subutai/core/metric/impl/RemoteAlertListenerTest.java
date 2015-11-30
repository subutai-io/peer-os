package io.subutai.core.metric.impl;


import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.metric.Alert;
import io.subutai.common.peer.Payload;


@RunWith( MockitoJUnitRunner.class )
@Ignore
public class RemoteAlertListenerTest
{

    @Mock
    MonitorImpl monitor;
    @Mock
    Payload payload;
    @Mock
    Alert alert;


    RemoteAlertListener remoteAlertListener;


    @Before
    public void setUp() throws Exception
    {
        remoteAlertListener = new RemoteAlertListener( monitor );
    }


//    @Test
//    public void testOnMessage() throws Exception
//    {
//        when( payload.getMessage( Alert.class ) ).thenReturn( alert );
//
//        remoteAlertListener.onRequest( payload );
//
//        verify( monitor ).notifyOnAlert( alert );
//
//        MonitorException exception = mock( MonitorException.class );
//
//        doThrow( exception ).when( monitor ).notifyOnAlert( alert );
//
//        remoteAlertListener.onRequest( payload );
//
//        verify( exception ).printStackTrace( any( PrintStream.class ) );
//    }
}
