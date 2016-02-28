package io.subutai.core.metric.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.metric.ResourceHostMetric;
import io.subutai.common.metric.ResourceHostMetrics;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.common.metric.ContainerHostMetric;
import io.subutai.core.metric.api.Monitor;

import com.google.common.collect.Sets;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for MetricListCommand
 */
@RunWith( MockitoJUnitRunner.class )
public class MetricsListCommandTest extends SystemOutRedirectTest
{
    @Mock
    Monitor monitor;
    @Mock
    ResourceHostMetrics resourceHostMetrics;

    private static final String METRIC_TO_STRING = "metrics";
    private MetricListCommand metricListCommand;


    @Before
    public void setUp() throws Exception
    {
        ResourceHostMetric metric = mock( ResourceHostMetric.class );
        when( metric.toString() ).thenReturn( METRIC_TO_STRING );
        when( resourceHostMetrics.getResources() ).thenReturn( Sets.newHashSet( metric ) );
        when( monitor.getResourceHostMetrics() ).thenReturn( resourceHostMetrics );
        metricListCommand = new MetricListCommand( monitor );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMonitor() throws Exception
    {
        new MetricListCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        metricListCommand.doExecute();

        assertThat( getSysOut(), containsString( METRIC_TO_STRING ) );
    }

}
