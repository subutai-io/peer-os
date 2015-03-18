package org.safehaus.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.common.util.JsonUtil;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Test for Metric
 */
public class MetricTest
{

    private static final String METRIC_JSON = " {\"host\":\"test\", \"totalRam\":\"123\"," +
            "\"availableRam\":\"123\", \"usedRam\":\"123\", \"usedCpu\":\"123\","
            + "  \"availableDisk\" : \"123\", \"usedDisk\" : \"123\", \"totalDisk\" : \"123\"}";
    private static final String HOST = "test";
    private static final double VALUE = 123;
    ContainerHostMetricImpl metric = JsonUtil.fromJson( METRIC_JSON, ContainerHostMetricImpl.class );


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( HOST, metric.getHost() );
        assertTrue( VALUE == metric.getTotalRam() );
        assertTrue( VALUE == metric.getAvailableRam() );
        assertTrue( VALUE == metric.getUsedRam() );
        assertTrue( VALUE == metric.getUsedCpu() );
    }


    @Test
    public void testToString() throws Exception
    {
        UUID environmentId = UUID.randomUUID();
        metric.setEnvironmentId( environmentId );

        assertThat( metric.toString(), containsString( environmentId.toString() ) );
    }


    @Test
    public void testOutput() throws Exception
    {
        metric.setEnvironmentId( UUID.randomUUID() );
        metric.setHostId( UUID.randomUUID() );

        System.out.println(metric);

    }
}
