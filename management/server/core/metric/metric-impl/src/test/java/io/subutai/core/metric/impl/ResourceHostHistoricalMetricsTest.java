package io.subutai.core.metric.impl;


import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import org.apache.cxf.helpers.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.common.metric.HistoricalMetrics;
import io.subutai.common.metric.SeriesBatch;
import io.subutai.common.metric.SeriesHelper;


/**
 * Test for ResourceHostMetric
 */
public class ResourceHostHistoricalMetricsTest
{
    private ObjectMapper mapper = new ObjectMapper();

    private HistoricalMetrics metric;


    @Before
    public void setUp() throws Exception
    {
        InputStream is =
                ResourceHostHistoricalMetricsTest.class.getClassLoader().getResourceAsStream( "rh-metric.dat" );

        String metricString = IOUtils.readStringFromStream( is );

        metric = mapper.readValue( metricString, HistoricalMetrics.class );
    }


    @Test
    public void testGetTagValues() throws Exception
    {
        System.out.println(
                SeriesHelper.getTagValues( metric.getSeriesMap().get( SeriesBatch.SeriesType.NET ), "iface" ) );
        System.out.println(
                SeriesHelper.getTagValues( metric.getSeriesMap().get( SeriesBatch.SeriesType.DISK ), "mount" ) );
    }
}
