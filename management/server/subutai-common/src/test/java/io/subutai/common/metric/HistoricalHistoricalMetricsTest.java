package io.subutai.common.metric;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.common.peer.Host;

import static org.junit.Assert.assertNotNull;


@RunWith( MockitoJUnitRunner.class )
public class HistoricalHistoricalMetricsTest
{
    private HistoricalMetric historicalMetric;

    @Mock
    Host host;


    @Before
    public void setUp() throws Exception
    {
        historicalMetric = new HistoricalMetric( host, MetricType.CPU, 5, 5 );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertNotNull( historicalMetric.getHost() );
        assertNotNull( historicalMetric.getMetricType() );
        assertNotNull( historicalMetric.getTimestamp() );
        assertNotNull( historicalMetric.getValue() );
    }
}