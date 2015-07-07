package io.subutai.core.metric.impl;


import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.core.metric.impl.ContainerHostMetricImpl;
import io.subutai.core.metric.impl.ContainerHostMetricRequest;
import io.subutai.core.metric.impl.ContainerHostMetricResponse;

import static org.junit.Assert.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricRequestResponseTest
{

    private static final UUID ENVIRONMENT_ID = UUID.randomUUID();
    ContainerHostMetricRequest request;
    ContainerHostMetricResponse response;
    @Mock
    Set<ContainerHostMetricImpl> metrics;


    @Before
    public void setUp() throws Exception
    {

        request = new ContainerHostMetricRequest( ENVIRONMENT_ID );

        response = new ContainerHostMetricResponse( metrics );
    }


    @Test
    public void testRequest() throws Exception
    {

        assertEquals( ENVIRONMENT_ID, request.getEnvironmentId() );
    }


    @Test
    public void testResponse() throws Exception
    {

        assertEquals( metrics, response.getMetrics() );
    }
}
