package io.subutai.core.hostregistry.api;


import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class ExceptionsTest
{
    private static final String MSG = "ERR";


    @Test
    public void testHostDisconnectedException() throws Exception
    {

        HostDisconnectedException exception2 = new HostDisconnectedException( MSG );

        assertEquals( MSG, exception2.getMessage() );
    }
}
