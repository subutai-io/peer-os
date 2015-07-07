package io.subutai.core.peer.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.core.peer.api.Payload;
import io.subutai.core.peer.impl.EchoRequestListener;

import static org.mockito.Mockito.verify;


@RunWith( MockitoJUnitRunner.class )
public class EchoRequestListenerTest
{
    EchoRequestListener listener;

    @Mock
    Payload payload;

    @Before
    public void setUp() throws Exception
    {
        listener = new EchoRequestListener();

    }


    @Test
    public void testOnRequest() throws Exception
    {

        listener.onRequest( payload );

        verify(payload).getMessage( String.class );
    }
}
