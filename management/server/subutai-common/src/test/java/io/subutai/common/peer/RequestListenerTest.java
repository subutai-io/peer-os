package io.subutai.common.peer;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Payload;
import io.subutai.common.peer.RequestListener;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class RequestListenerTest
{
    private static final String RECIPIENT = "recipient";

    @Mock
    Payload payload;

    RequestListener requestListener;


    static class RequestListenerImpl extends RequestListener
    {
        protected RequestListenerImpl( final String recipient )
        {
            super( recipient );
        }


        @Override
        public Object onRequest( final Payload payload ) throws Exception
        {
            return payload;
        }
    }


    @Before
    public void setUp() throws Exception
    {
        requestListener = new RequestListenerImpl( RECIPIENT );
    }


    @Test
    public void testGetRecipient() throws Exception
    {
        assertEquals( RECIPIENT, requestListener.getRecipient() );
    }


    @Test
    public void testOnRequest() throws Exception
    {
        Object response = requestListener.onRequest( payload );

        assertEquals( payload, response );
    }
}
