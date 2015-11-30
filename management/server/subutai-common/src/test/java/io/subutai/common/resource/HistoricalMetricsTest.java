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
    HistoricalMetrics historicalMetrics;


    @Before
    public void setUp()
    {
        objectMapper = new ObjectMapper();
    }


    @Test
    public void testReadSeries() throws IOException
    {
        historicalMetrics = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                HistoricalMetrics.class );
    }


    @Test
    public void testNotNullSeries() throws IOException
    {
        historicalMetrics = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                HistoricalMetrics.class );
        assertNotNull( historicalMetrics.getMetrics() );
    }


    @Test
    public void testSeriesCount() throws IOException
    {
        historicalMetrics = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                HistoricalMetrics.class );
        assertEquals( 4, historicalMetrics.getMetrics().length );
    }


    @Test
    public void testSeriesLxcCpu() throws IOException
    {
        historicalMetrics = objectMapper.readValue( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "lxc.txt" ),
                HistoricalMetrics.class );
        SeriesBatch batch = historicalMetrics.getMetrics()[0];
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
