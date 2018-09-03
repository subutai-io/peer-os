package io.subutai.common.metric;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class SeriesHelperTest
{
    private HistoricalMetrics rhMetrics;
    private HistoricalMetrics chMetrics;


    @Before
    public void setup() throws IOException
    {
        final ObjectMapper objectMapper = new ObjectMapper();

        final String rhJson = IOUtils.toString(
                SeriesHelperTest.class.getClassLoader().getResourceAsStream( "metrics/resource_host_metrics.json" ),
                StandardCharsets.UTF_8 );
        rhMetrics = objectMapper.readValue( rhJson, HistoricalMetrics.class );
        final String chJson = IOUtils.toString(
                SeriesHelperTest.class.getClassLoader().getResourceAsStream( "metrics/container_host_metrics.json" ),
                StandardCharsets.UTF_8 );
        chMetrics = objectMapper.readValue( chJson, HistoricalMetrics.class );
    }


    @Test
    public void testGetAvg()
    {
        final List<Series> cpuSeries = rhMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU );

        assertEquals( 0.9345833333, SeriesHelper.getAvg( cpuSeries, new Tag( "type", "system" ) ), 0.0000001 );
        assertEquals( 3.005, SeriesHelper.getAvg( cpuSeries, new Tag( "type", "user" ) ), 0.0000001 );

        final List<Series> netSeries = rhMetrics.getSeriesByType( SeriesBatch.SeriesType.NET );

        assertEquals( 853730.013333333,
                SeriesHelper.getAvg( netSeries, new Tag( "iface", "eth0" ), new Tag( "type", "in" ) ), 0.0000001 );
    }


    @Test
    public void testGetLast()
    {
        final List<Series> cpuSeries = rhMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU );
        assertEquals( 0.7833333333333333, SeriesHelper.getLast( cpuSeries, new Tag( "type", "system" ) ), 0.0000001 );
        assertEquals( 1.9333333333333333, SeriesHelper.getLast( cpuSeries, new Tag( "type", "user" ) ), 0.0000001 );


        final List<Series> netSeries = rhMetrics.getSeriesByType( SeriesBatch.SeriesType.NET );
        assertEquals( 36114.6, SeriesHelper.getLast( netSeries, new Tag( "iface", "eth0" ), new Tag( "type", "in" ) ),
                0.0000001 );

        final List<Series> diskSeries = rhMetrics.getSeriesByType( SeriesBatch.SeriesType.DISK );
        Double available = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/" ), new Tag( "type", "available" ) );
        assertEquals( 3.00056576e+08, available, 0.0000001 );

        Double used = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/" ), new Tag( "type", "used" ) );
        assertEquals( 6.52783616e+08, used, 0.0000001 );

        Double total = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/" ), new Tag( "type", "total" ) );
        assertEquals( 1.02330368e+09, total, 0.0000001 );

        //        System.out.println( String.format( "%f = %f %f", total, available + used,total- available - used ) );
        //        assertEquals( total, available + used, 1024 * 1024 );


        available = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/mnt" ), new Tag( "type", "available" ) );
        assertEquals( 1.01273182208e+11, available, 0.0000001 );

        used = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/mnt" ), new Tag( "type", "used" ) );
        assertEquals( 5.54901504e+09, used, 0.0000001 );

        total = SeriesHelper.getLast( diskSeries, new Tag( "mount", "/mnt" ), new Tag( "type", "total" ) );
        assertEquals( 1.073741824e+11, total, 0.0000001 );

        //        System.out.println( String.format( "%f = %f %f", total, available + used,total- available - used ) );
        //        assertEquals( total, available + used, 1024 * 1024 );

        assertNull( SeriesHelper
                .getLast( new ArrayList<Series>(), new Tag( "mount", "/mnt" ), new Tag( "type", "available" ) ) );
    }
}
