package org.safehaus.subutai.common.peer;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith( MockitoJUnitRunner.class )
public class PeerExceptionTest
{
    private PeerException peerException;


    @Before
    public void setUp() throws Exception
    {
        peerException = new PeerException( new Throwable(  ) );
        peerException = new PeerException( "exception" );
        peerException = new PeerException( "exception", "description" );
        peerException = new PeerException( "exception", new Throwable(  ) );
    }


    @Test
    public void testToString() throws Exception
    {
        peerException.toString();
    }
}