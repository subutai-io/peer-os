package io.subutai.common.peer;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import io.subutai.common.peer.Payload;

import static junit.framework.Assert.assertNull;
import static junit.framework.TestCase.assertEquals;


public class PayloadTest
{

    Payload payload;

    String peerId = UUID.randomUUID().toString();
    Request request = new Request( peerId );


    static class Request
    {
        private String peerId;


        public Request( final String peerId )
        {
            this.peerId = peerId;
        }


        @Override
        public boolean equals( final Object o )
        {
            if ( this == o )
            {
                return true;
            }
            if ( !( o instanceof Request ) )
            {
                return false;
            }

            final Request request = ( Request ) o;

            if ( !peerId.equals( request.peerId ) )
            {
                return false;
            }

            return true;
        }


        @Override
        public int hashCode()
        {
            return peerId.hashCode();
        }
    }


    @Before
    public void setUp() throws Exception
    {
        payload = new Payload( request, peerId );
    }


    @Test
    public void testGetMessage() throws Exception
    {
        Request request2 = payload.getMessage( Request.class );

        assertEquals( request, request2 );
    }


//    @Test
//    public void testGetMessage2() throws Exception
//    {
//        payload.request = null;
//
//        assertNull( payload.getMessage( Request.class ) );
//    }


    @Test
    public void testGetSourcePeerId() throws Exception
    {
        assertEquals( peerId, payload.getSourcePeerId() );
    }
}
