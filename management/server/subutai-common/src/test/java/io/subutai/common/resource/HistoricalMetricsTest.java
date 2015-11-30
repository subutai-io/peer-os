package io.subutai.common.resource;


import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class HistoricalMetricsTest
{
    ObjectMapper objectMapper;
    Metric metric;


    @Before
    public void setUp()
    {
        objectMapper = new ObjectMapper();
    }


    @Test
    public void testReadSeries() throws IOException
    {
        metric = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                Metric.class );
    }


    @Test
    public void testNotNullSeries() throws IOException
    {
        metric = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                Metric.class );
        assertNotNull( metric.getMetrics() );
    }


    @Test
    public void testSeriesCount() throws IOException
    {
        metric = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                Metric.class );
        assertEquals( 4, metric.getMetrics().length );
    }


    @Test
    public void testSeriesLxcCpu() throws IOException
    {
        metric = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                Metric.class );
        SeriesBatch batch = metric.getMetrics()[0];
        Series lxcCpuSeries = batch.getSeries()[0];
        assertEquals( "lxc_cpu", lxcCpuSeries.name );
        assertEquals( 1, lxcCpuSeries.tags.size() );
        assertEquals( "system", lxcCpuSeries.tags.get( "type" ) );
        assertNotNull( lxcCpuSeries.getValues() );
        assertTrue( lxcCpuSeries.getValues().size() > 0 );
        assertEquals( 2, lxcCpuSeries.getValues().get( 0 ).size() );
        assertEquals( "2015-11-30T03:02:00Z", lxcCpuSeries.getValues().get( 0 ).get(0) );
        assertEquals( "0", lxcCpuSeries.getValues().get( 0 ).get(1) );
    }
}
