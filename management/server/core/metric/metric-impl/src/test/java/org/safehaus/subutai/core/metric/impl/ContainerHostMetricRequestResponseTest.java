package org.safehaus.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;

import com.google.common.collect.Sets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ContainerHostMetricRequestResponseTest
{


    @Test
    public void testRequest() throws Exception
    {
        UUID environmentId = UUID.randomUUID();

        ContainerHostMetricRequest request = new ContainerHostMetricRequest( environmentId );

        assertEquals( environmentId, request.getEnvironmentId() );
        assertNotNull( request.getId() );
    }


    @Test
    public void testResponse() throws Exception
    {
        Set<ContainerHostMetric> metrics = Sets.newHashSet();
        UUID requestId = UUID.randomUUID();

        ContainerHostMetricResponse response = new ContainerHostMetricResponse( requestId, metrics );

        assertEquals( metrics, response.getMetrics() );
        assertEquals( requestId, response.getRequestId() );
    }
}
