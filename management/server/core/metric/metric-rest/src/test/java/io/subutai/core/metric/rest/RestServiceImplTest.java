package io.subutai.core.metric.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;
import io.subutai.core.metric.impl.ContainerHostMetricImpl;
import io.subutai.core.metric.impl.ResourceHostMetricImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Test for RestServiceImpl
 */
@RunWith( MockitoJUnitRunner.class )
public class RestServiceImplTest
{
    @Mock
    Monitor monitor;
    @Mock
    EnvironmentManager environmentManager;
    RestServiceImpl restService;
    ResourceHostMetric resourceHostMetric;
    ContainerHostMetric containerHostMetric;


    @Before
    public void setUp() throws Exception
    {
        monitor = mock( Monitor.class );
        environmentManager = mock( EnvironmentManager.class );
        resourceHostMetric = new ResourceHostMetricImpl();
        containerHostMetric = new ContainerHostMetricImpl();
        when( monitor.getResourceHostsMetrics() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );
        restService = new RestServiceImpl( monitor, environmentManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMonitor() throws Exception
    {
        new RestServiceImpl( null, environmentManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullEnvironmentManager() throws Exception
    {
        new RestServiceImpl( monitor, null );
    }


    @Test
    public void testGetResourceHostMetrics() throws Exception
    {

        Response response = restService.getResourceHostsMetrics();

        Set<ResourceHostMetric> metrics = JsonUtil.fromJson( response.getEntity().toString(),
                new TypeToken<Set<ResourceHostMetricImpl>>() {}.getType() );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertFalse( metrics.isEmpty() );
    }


    @Test
    public void testGetResourceHostMetricsException() throws Exception
    {
        when( monitor.getResourceHostsMetrics() ).thenThrow( new RuntimeException( "" ) );

        Response response = restService.getResourceHostsMetrics();

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testAlertThresholdExcess() throws Exception
    {
        String alertMetric = JsonUtil.toJson( containerHostMetric );
        Response response = restService.alert( alertMetric );

        verify( monitor ).alert( alertMetric );

        assertEquals( Response.Status.ACCEPTED.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testAlertThresholdExcessException() throws Exception
    {
        String alertMetric = JsonUtil.toJson( containerHostMetric );
        doThrow( new MonitorException( "" ) ).when( monitor ).alert( alertMetric );

        Response response = restService.alert( alertMetric );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerHostMetrics() throws Exception
    {
        String environmentId = UUID.randomUUID().toString();
        Environment environment = mock( Environment.class );
        when( environmentManager.loadEnvironment( environmentId ) ).thenReturn( environment );
        when( monitor.getContainerHostsMetrics( environment ) ).thenReturn( Sets.newHashSet( containerHostMetric ) );

        Response response = restService.getContainerHostsMetrics( environmentId.toString() );
        Set<ContainerHostMetric> metrics = JsonUtil.fromJson( response.getEntity().toString(),
                new TypeToken<Set<ContainerHostMetricImpl>>() {}.getType() );

        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertFalse( metrics.isEmpty() );
    }


    @Test
    public void testGetContainerHostMetricsWithNullEnvironment() throws Exception
    {
        String environmentId = UUID.randomUUID().toString();
        doThrow( new EnvironmentNotFoundException( null ) ).when( environmentManager ).loadEnvironment( environmentId );

        Response response = restService.getContainerHostsMetrics( environmentId.toString() );

        assertEquals( Response.Status.NOT_FOUND.getStatusCode(), response.getStatus() );
    }


    @Test
    public void testGetContainerHostMetricsWithMonitorException() throws Exception
    {
        String environmentId = UUID.randomUUID().toString();
        Environment environment = mock( Environment.class );
        when( environmentManager.loadEnvironment( environmentId ) ).thenReturn( environment );
        when( monitor.getContainerHostsMetrics( environment ) ).thenThrow( new MonitorException( "" ) );

        Response response = restService.getContainerHostsMetrics( environmentId.toString() );

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    @Test( expected = IllegalArgumentException.class )
    public void testGetContainerHostMetricsWithIllegalEnvironmentId() throws Exception
    {
        restService.getContainerHostsMetrics( null );
    }
}
