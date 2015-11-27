package io.subutai.core.metric.rest;


import java.util.Set;
import java.util.UUID;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.peer.ContainerId;
import io.subutai.common.metric.ResourceAlertValue;
import io.subutai.common.metric.ResourceAlert;
import io.subutai.common.resource.MeasureUnit;
import io.subutai.common.resource.ResourceType;
import io.subutai.common.resource.ResourceValue;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.core.metric.api.Monitor;
import io.subutai.core.metric.api.MonitorException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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
    ResourceAlertValue alertValue;
    @Mock
    ResourceHostMetrics resourceHostMetrics;
    @Mock
    private ContainerId containerId;

    @Mock
    ResourceValue currentValue;

    @Mock
    ResourceValue quotaValue;


    @Before
    public void setUp() throws Exception
    {
        monitor = mock( Monitor.class );
        environmentManager = mock( EnvironmentManager.class );
        alertValue = new ResourceAlert( containerId, ResourceType.RAM, currentValue, quotaValue );
        resourceHostMetric = new ResourceHostMetric();
        when( resourceHostMetrics.getResources() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );
        when( monitor.getResourceHostMetrics() ).thenReturn( resourceHostMetrics );
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
    @Ignore
    public void testGetResourceHostMetrics() throws Exception
    {

        Response response = restService.getResourceHostsMetrics();

        ResourceHostMetrics metrics = JsonUtil.fromJson( response.getEntity().toString(), ResourceHostMetrics.class );
        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
        assertFalse( metrics.isEmpty() );
    }


    @Test
    public void testGetResourceHostMetricsException() throws Exception
    {
        when( monitor.getResourceHostMetrics() ).thenThrow( new RuntimeException( "" ) );

        Response response = restService.getResourceHostsMetrics();

        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    }


    //    @Test
    //    public void testAlertThresholdExcess() throws Exception
    //    {
    ////        AlertValue alertMetric = JsonUtil.toJson( alertValue );
    //        Response response = restService.alert( alertValue );
    //
    //        verify( monitor ).alert( alertValue );
    //
    //        assertEquals( Response.Status.ACCEPTED.getStatusCode(), response.getStatus() );
    //    }
    //
    //
    //    @Test
    //    public void testAlertThresholdExcessException() throws Exception
    //    {
    //        AlertValue alertMetric = JsonUtil.toJson( alertValue );
    //        doThrow( new MonitorException( "" ) ).when( monitor ).alert( alertMetric );
    //
    //        Response response = restService.alert( alertMetric );
    //
    //        assertEquals( Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus() );
    //    }


    //    @Test
    //    public void testGetContainerHostMetrics() throws Exception
    //    {
    //        String environmentId = UUID.randomUUID().toString();
    //        Environment environment = mock( Environment.class );
    //        when( environmentManager.loadEnvironment( environmentId ) ).thenReturn( environment );
    //        when( monitor.getContainerHostsMetrics( environment ) ).thenReturn( Sets.newHashSet( alertValue ) );
    //
    //        Response response = restService.getContainerHostsMetrics( environmentId.toString() );
    //        Set<ContainerHostMetric> metrics = JsonUtil.fromJson( response.getEntity().toString(),
    //                new TypeToken<Set<ContainerHostMetricImpl>>() {}.getType() );
    //
    //        assertEquals( Response.Status.OK.getStatusCode(), response.getStatus() );
    //        assertFalse( metrics.isEmpty() );
    //    }


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
