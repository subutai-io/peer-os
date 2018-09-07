package io.subutai.common.metric;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.subutai.bazaar.share.dto.metrics.CpuDto;
import io.subutai.bazaar.share.dto.metrics.DiskDto;
import io.subutai.bazaar.share.dto.metrics.HostMetricsDto;
import io.subutai.bazaar.share.dto.metrics.MemoryDto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class HistoricalMetricsTest
{
    private HistoricalMetrics rhMetrics;
    private HistoricalMetrics chMetrics;


    @Before
    public void setup() throws IOException
    {
        final ObjectMapper objectMapper = new ObjectMapper();

        final String rhJson = IOUtils.toString( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream(
                "metrics/resource_host_metrics.json" ), StandardCharsets.UTF_8 );
        rhMetrics = objectMapper.readValue( rhJson, HistoricalMetrics.class );
        Calendar cal = Calendar.getInstance();
        Date endTime = cal.getTime();
        cal.add( Calendar.MINUTE, -15 );
        Date startTime = cal.getTime();
        rhMetrics.setStartTime( startTime );
        rhMetrics.setEndTime( endTime );
        final String chJson = IOUtils.toString( HistoricalMetricsTest.class.getClassLoader().getResourceAsStream(
                "metrics/container_host_metrics.json" ), StandardCharsets.UTF_8 );
        chMetrics = objectMapper.readValue( chJson, HistoricalMetrics.class );
    }


    @Test
    public void testGetHostType()
    {
        assertEquals( HostMetricsDto.HostType.CONTAINER_HOST, chMetrics.getHostType() );
        assertEquals( HostMetricsDto.HostType.RESOURCE_HOST, rhMetrics.getHostType() );
    }


    @Test
    public void testGetSeriesByType()
    {
        // count(system, user, iowait, nice, idle) == 5
        assertEquals( 5, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );
        // count(system, user) == 2
        assertEquals( 2, chMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );
        // count(Active, Buffers, MemFree, Cached) == 4
        assertEquals( 4, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.MEMORY ).size() );
        // count(cache, rss) == 2
        assertEquals( 2, chMetrics.getSeriesByType( SeriesBatch.SeriesType.CPU ).size() );

        assertEquals( 15, rhMetrics.getSeriesByType( SeriesBatch.SeriesType.DISK ).size() );
    }


    @Test
    public void testCpu()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        CpuDto cpuDto = metrics.getCpu();

        assertNotNull( cpuDto );

        assertEquals( 0.9345833333, cpuDto.getSystem(), 0.0001 );
        assertEquals( 3.005, cpuDto.getUser(), 0.0001 );
        assertEquals( 0.0, cpuDto.getNice(), 0.0001 );
        assertEquals( 95.26708333333335, cpuDto.getIdle(), 0.0001 );
        assertEquals( 0.04125000000000002, cpuDto.getIowait(), 0.0001 );
    }


    @Test
    public void testMemory()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        MemoryDto memoryDto = metrics.getMemory();

        assertNotNull( memoryDto );

        assertEquals( 1440642129.26984, memoryDto.getActive(), 0.0001 );
        assertEquals( 6414672749.71429, memoryDto.getMemFree(), 0.0001 );
        assertEquals( 905762913.52381, memoryDto.getCached(), 0.0001 );
        assertEquals( 17029022.4761905, memoryDto.getBuffers(), 0.0001 );
    }


    @Test
    public void testDisk()
    {
        final HostMetricsDto metrics = rhMetrics.getHostMetrics();

        Map<String, DiskDto> disk = metrics.getDisk();

        assertNotNull( disk );
        assertEquals( 5, disk.keySet().size() );

        // root partition
        DiskDto root = disk.get( "/" );
        double available = root.getAvailable();
        assertEquals( 3.00056576E8, available, 0.0001 );
        double total = root.getTotal();
        assertEquals( 1.02330368E9, total, 0.0001 );
        double used = root.getUsed();
        assertEquals( 6.52783616E8, used, 0.0001 );

        // TODO: 10/29/16 why total <> available + used
        //        assertEquals( total, available+used,0.001 );
    }
}
