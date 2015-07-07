package io.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Test;

import io.subutai.core.metric.impl.ContainerHostMetricImpl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Test for ContainerHostMetric
 */
public class ContainerHostMetricTest
{
    @Test
    public void testSetEnvironmentId() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        ContainerHostMetricImpl containerHostMetric = new ContainerHostMetricImpl();

        containerHostMetric.setEnvironmentId( environmentId );

        assertEquals( environmentId, containerHostMetric.getEnvironmentId() );
    }


    @Test
    public void testToString() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        ContainerHostMetricImpl containerHostMetric = new ContainerHostMetricImpl();

        containerHostMetric.setEnvironmentId( environmentId );

        assertThat( containerHostMetric.toString(), containsString( environmentId.toString() ) );
    }
}
