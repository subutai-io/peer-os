package org.safehaus.subutai.core.monitor.cli;


import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.safehaus.subutai.core.monitor.api.Metric;
import org.safehaus.subutai.core.monitor.api.Monitoring;

import org.apache.commons.lang3.time.DateUtils;

import com.google.gson.reflect.TypeToken;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by talas on 10/3/14.
 */
public class AllMetricsCommandTest
{
    protected String hostname = null;
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
        Type mapType = new TypeToken<Map<Metric, Map<Date, Double>>>()
        {
        }.getType();

        Map<Metric, Map<Date, Double>> data = new HashMap<>();
        Map<Metric, Map<Date, Double>> spy = spy( data );
        when( monitoring.getDataForAllMetrics( hostname, any( Date.class ), any( Date.class ) ) ).thenReturn( spy );
        allMetricsCommand.doExecute();
        verify( monitoring ).getDataForAllMetrics( hostname, any( Date.class ), any( Date.class ) );
    }


    protected Object doExecute()
    {

        Date endDate = new Date();
        Date startDate = DateUtils.addDays( endDate, -1 );

        Map<Metric, Map<Date, Double>> data = monitoring.getDataForAllMetrics( hostname, startDate, endDate );

        System.out.println( "Data: " + data );

        return null;
    }
}
