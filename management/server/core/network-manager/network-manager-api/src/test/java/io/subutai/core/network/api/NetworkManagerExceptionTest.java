package io.subutai.core.network.api;


import org.junit.Test;
import io.subutai.core.network.api.NetworkManagerException;

import static org.junit.Assert.assertEquals;


public class NetworkManagerExceptionTest
{
    private static final String MSG = "ERR";


    @Test
    public void testException() throws Exception
    {
        Exception cause = new Exception();

        NetworkManagerException networkManagerException = new NetworkManagerException( cause );

        assertEquals( cause, networkManagerException.getCause() );

        NetworkManagerException networkManagerException2 = new NetworkManagerException( MSG );

        assertEquals( MSG, networkManagerException2.getMessage() );
    }
}
