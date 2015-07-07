package io.subutai.core.metric.api;


import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.metric.api.ContainerHostMetric;
import io.subutai.core.metric.api.MonitorException;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricTest
{
    private static final class ContainerHostMetricImpl extends ContainerHostMetric {}


    private ContainerHostMetric metric;
    private UUID envId = UUID.randomUUID();
    private static final String ERR_MSG = "ERR";


    @Before
    public void setUp() throws Exception
    {

        metric = new ContainerHostMetricImpl();

        metric.environmentId = envId;
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {


        assertEquals( metric.getEnvironmentId(), envId );
    }


    @Test
    public void testToString() throws Exception
    {

        assertThat( metric.toString(), containsString( envId.toString() ) );
    }


    @Test
    public void testMonitorException() throws Exception
    {
        Exception nestedException = new Exception();

        MonitorException monitorException = new MonitorException( nestedException );

        Assert.assertEquals( nestedException, monitorException.getCause() );

        monitorException = new MonitorException( ERR_MSG );

        Assert.assertEquals( ERR_MSG, monitorException.getMessage() );
    }
}
