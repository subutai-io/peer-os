package io.subutai.core.localpeer.impl;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;


import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class PeerInitializationErrorTest
{
    private static final String ERR_MSG = "error";
    @Mock
    RuntimeException cause;

    LocalPeerInitializationError error;


    @Before
    public void setUp() throws Exception
    {
        error = new LocalPeerInitializationError( ERR_MSG, cause );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( ERR_MSG, error.getMessage() );
        assertEquals( cause, error.getCause() );
    }
}
