package org.safehaus.subutai.core.metric.cli;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.common.test.SystemOutRedirectTest;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.common.metric.ResourceHostMetric;

import com.google.common.collect.Sets;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for ResourceHostMetricsCommand
 */
@RunWith( MockitoJUnitRunner.class )
public class ResourceHostMetricsCommandTest extends SystemOutRedirectTest
{
    @Mock
    Monitor monitor;

    private static final String METRIC_TO_STRING = "metric";

    private ResourceHostMetricsCommand resourceHostMetricsCommand;


    @Before
    public void setUp() throws Exception
    {
        ResourceHostMetric resourceHostMetric = mock( ResourceHostMetric.class );
        when( resourceHostMetric.toString() ).thenReturn( METRIC_TO_STRING );
        when( monitor.getResourceHostsMetrics() ).thenReturn( Sets.newHashSet( resourceHostMetric ) );
        resourceHostMetricsCommand = new ResourceHostMetricsCommand( monitor );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMonitor() throws Exception
    {
        new ResourceHostMetricsCommand( null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        resourceHostMetricsCommand.doExecute();

        assertThat( getSysOut(), containsString( METRIC_TO_STRING ) );
    }
}
