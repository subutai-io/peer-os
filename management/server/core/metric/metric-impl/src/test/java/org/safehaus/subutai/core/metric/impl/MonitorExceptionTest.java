package org.safehaus.subutai.core.metric.impl;


import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.MonitorException;

import static org.junit.Assert.assertEquals;


/**
 * Test for MonitorException
 */
public class MonitorExceptionTest
{
    @Test
    public void testNestedException() throws Exception
    {
        Exception nestedException = new Exception();
        MonitorException monitorException = new MonitorException( nestedException );

        assertEquals( nestedException, monitorException.getCause() );
    }
}
