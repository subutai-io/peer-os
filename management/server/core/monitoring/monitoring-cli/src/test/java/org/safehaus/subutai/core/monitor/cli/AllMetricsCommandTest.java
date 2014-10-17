package org.safehaus.subutai.core.monitor.cli;


import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.MetricType;
import org.safehaus.subutai.core.monitor.api.Monitoring;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


/**
 * Created by talas on 10/3/14.
 */
public class AllMetricsCommandTest
{
    protected String hostname = "hostname";
    private Monitoring monitoring;
    private AllMetricsCommand allMetricsCommand;


    @Before
    public void setupClasses()
    {
        monitoring = mock( Monitoring.class );
        allMetricsCommand = new AllMetricsCommand();
        allMetricsCommand.setMonitoring( monitoring );
    }


    @Test(expected = NullPointerException.class)
    public void shouldThrowNullPointerExceptionOnSetMonitoring()
    {
        allMetricsCommand.setMonitoring( null );
    }


    @Test
    public void shouldAccessMonitoringGetDataForAllMetricsOnDoExecute()
    {
        Map<MetricType, Map<Date, Double>> data = any();
//        when( monitoring.getDataForAllMetrics( hostname, any( Date.class ), any( Date.class ) ) ).thenReturn( data );
//        allMetricsCommand.doExecute();
//        verify( monitoring ).getDataForAllMetrics( any( String.class ), any( Date.class ), any( Date.class ) );
    }
}
