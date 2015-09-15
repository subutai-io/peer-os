package io.subutai.core.metric.api;


import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Objects;

import io.subutai.common.metric.ContainerHostMetric;

import static junit.framework.TestCase.assertEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricTest
{
    private static class ContainerHostMetricImpl extends ContainerHostMetric
    {
        @Override
        public String toString()
        {
            return Objects.toStringHelper( this ).add( "metric", super.toString() ).add( "hostId", hostId )
                          .add( "environmentId", getEnvironmentId() ).toString();
        }
    }


    private ContainerHostMetric metric;
    private String envId = UUID.randomUUID().toString();
    private static final String ERR_MSG = "ERR";


    @Before
    public void setUp() throws Exception
    {

        metric = spy( new ContainerHostMetricImpl() );

        doReturn( envId ).when( metric ).getEnvironmentId();
    }


    @Test
    public void testGetEnvironmentId() throws Exception
    {


        assertEquals( metric.getEnvironmentId(), envId );
    }


    @Test
    public void testToString() throws Exception
    {

        assertThat( metric.toString(), containsString( envId ) );
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
