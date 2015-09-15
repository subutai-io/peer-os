package io.subutai.core.metric.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.environment.Environment;
import io.subutai.common.environment.EnvironmentNotFoundException;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.env.api.EnvironmentManager;
import io.subutai.core.metric.api.ContainerHostMetric;
import io.subutai.core.metric.api.Monitor;

import com.google.common.collect.Sets;

import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test for ContainerHostMetricsCommand
 */
@RunWith( MockitoJUnitRunner.class )
public class ContainerHostMetricsCommandTest extends SystemOutRedirectTest
{
    @Mock
    Monitor monitor;
    @Mock
    EnvironmentManager environmentManager;
    private static final String ENVIRONMENT_ID = UUID.randomUUID().toString();
    private static final String METRIC_TO_STRING = "metric";
    private static final String ENVIRONMENT_NOT_FOUND_MSG = "Environment not found";
    private ContainerHostMetricsCommand containerHostMetricsCommand;


    @Before
    public void setUp() throws Exception
    {
        Environment environment = mock( Environment.class );
        ContainerHostMetric metric = mock( ContainerHostMetric.class );
        when( metric.toString() ).thenReturn( METRIC_TO_STRING );
        when( monitor.getContainerHostsMetrics( environment ) ).thenReturn( Sets.newHashSet( metric ) );
        when( environmentManager.findEnvironment( ENVIRONMENT_ID ) ).thenReturn( environment );
        containerHostMetricsCommand = new ContainerHostMetricsCommand( monitor, environmentManager );
        containerHostMetricsCommand.environmentIdString = ENVIRONMENT_ID.toString();
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullMonitor() throws Exception
    {
        new ContainerHostMetricsCommand( null, environmentManager );
    }


    @Test( expected = NullPointerException.class )
    public void testConstructorShouldFailOnNullEnvironmentManager() throws Exception
    {
        new ContainerHostMetricsCommand( monitor, null );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        containerHostMetricsCommand.doExecute();

        assertThat( getSysOut(), containsString( METRIC_TO_STRING ) );
    }


    @Test
    public void testDoExecuteWithMissingEnvironment() throws Exception
    {
        doThrow( new EnvironmentNotFoundException( null ) ).when( environmentManager )
                                                           .findEnvironment( ENVIRONMENT_ID );

        containerHostMetricsCommand.doExecute();

        assertThat( getSysOut(), containsString( ENVIRONMENT_NOT_FOUND_MSG ) );
    }
}
