package io.subutai.core.metric.impl;


import org.junit.Test;
import io.subutai.core.metric.api.MonitorException;

import static org.junit.Assert.assertEquals;


/**
 * Test for MonitorException
 */
public class MonitorExceptionTest
{
    private static final String ERR_MSG = "OOPS";


    @Test
    public void testNestedException() throws Exception
    {
        Exception nestedException = new Exception();
        MonitorException monitorException = new MonitorException( nestedException );

        assertEquals( nestedException, monitorException.getCause() );
    }


    @Test
    public void testMessage() throws Exception
    {
        MonitorException monitorException = new MonitorException( ERR_MSG );

        assertEquals( ERR_MSG, monitorException.getMessage() );
    }
}
