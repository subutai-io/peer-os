package io.subutai.common.metric;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.dto.metrics.CpuDto;
import io.subutai.hub.share.dto.metrics.DiskDto;
import io.subutai.hub.share.dto.metrics.HostMetricsDto;
import io.subutai.hub.share.dto.metrics.MemoryDto;


/**
 * Historical metrics
 */
public class HistoricalMetrics
{
    @JsonProperty( "Metrics" )
    List<SeriesBatch> metrics = new ArrayList<>();
    private Map<SeriesBatch.SeriesType, List<Series>> seriesMap = new HashMap<>();


    public HistoricalMetrics()
    {
    }


    public HistoricalMetrics( @JsonProperty( "Metrics" ) final List<SeriesBatch> metrics )
    {
        this.metrics = metrics;
    }


    @JsonIgnore
    public List<SeriesBatch> getMetrics()
    {
        return metrics;
    }


    @JsonIgnore
    public HostMetricsDto.HostType getHostType()
    {
        HostMetricsDto.HostType result = null;
        if ( metrics.size() > 0 && metrics.get( 0 ).getSeries().size() > 0 )
        {
            if ( metrics.get( 0 ).getSeries().get( 0 ).getName().startsWith( "host_" ) )
            {
                result = HostMetricsDto.HostType.RESOURCE_HOST;
            }
            else
            {
                if ( metrics.get( 0 ).getSeries().get( 0 ).getName().startsWith( "lxc_" ) )
                {
                    result = HostMetricsDto.HostType.CONTAINER_HOST;
                }
            }
        }

        if ( result == null )
        {
            throw new IllegalStateException( "Could not determine host type." );
        }
        return result;
    }


    @JsonIgnore
    public List<Series> getSeriesByType( final SeriesBatch.SeriesType type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "Series type could not be null." );
        }

        List<Series> result = new ArrayList<>();
        final HostMetricsDto.HostType hostType = getHostType();

        for ( SeriesBatch batch : metrics )
        {
            result.addAll( batch.getSeriesByName( type.getName( hostType ) ) );
        }
        return result;
    }


    protected void splitSeries()
    {
        for ( SeriesBatch.SeriesType type : SeriesBatch.SeriesType.values() )
        {
            seriesMap.put( type, getSeriesByType( type ) );
        }
    }


    @JsonIgnore
    private CpuDto getCpuDto( final List<Series> series )
    {
        CpuDto cpuDto = new CpuDto();
        cpuDto.setSystem( SeriesHelper.getAvg( series, new Tag( "type", "system" ) ) );
        cpuDto.setUser( SeriesHelper.getAvg( series, new Tag( "type", "user" ) ) );
        cpuDto.setIdle( SeriesHelper.getAvg( series, new Tag( "type", "idle" ) ) );
        cpuDto.setIowait( SeriesHelper.getAvg( series, new Tag( "type", "iowait" ) ) );
        cpuDto.setNice( SeriesHelper.getAvg( series, new Tag( "type", "nice" ) ) );

        return cpuDto;
    }


    @JsonIgnore
    private MemoryDto getMemoryDto( final List<Series> series )
    {
        MemoryDto memoryDto = new MemoryDto();
        memoryDto.setActive( SeriesHelper.getAvg( series, new Tag( "type", "active" ) ) );
        memoryDto.setBuffers( SeriesHelper.getAvg( series, new Tag( "type", "buffers" ) ) );
        memoryDto.setCached( SeriesHelper.getAvg( series, new Tag( "type", "cached" ) ) );
        memoryDto.setMemFree( SeriesHelper.getAvg( series, new Tag( "type", "memfree" ) ) );
        return memoryDto;
    }


    @JsonIgnore
    private Map<String, DiskDto> getDiskDto( final List<Series> series )
    {
        Map<String, DiskDto> result = new HashMap<>();
        DiskDto root = new DiskDto();
        root.setAvailable( SeriesHelper.getAvg( series, new Tag( "type", "available" ), new Tag( "mount", "/" ) ) );
        root.setTotal( SeriesHelper.getAvg( series, new Tag( "type", "total" ), new Tag( "mount", "/" ) ) );
        root.setUsed( SeriesHelper.getAvg( series, new Tag( "type", "used" ), new Tag( "mount", "/" ) ) );

        DiskDto mnt = new DiskDto();
        mnt.setAvailable( SeriesHelper.getAvg( series, new Tag( "type", "available" ), new Tag( "mount", "/mnt" ) ) );
        mnt.setTotal( SeriesHelper.getAvg( series, new Tag( "type", "total" ), new Tag( "mount", "/mnt" ) ) );
        mnt.setUsed( SeriesHelper.getAvg( series, new Tag( "type", "used" ), new Tag( "mount", "/mnt" ) ) );

        result.put( "/", root );
        result.put( "/mnt", mnt );
        return result;
    }


    @JsonIgnore
    public HostMetricsDto getHostMetrics()
    {
        HostMetricsDto result = new HostMetricsDto();

        HostMetricsDto.HostType hostType = getHostType();
        result.setType( hostType );
        splitSeries();

        result.setCpuDto( getCpuDto( seriesMap.get( SeriesBatch.SeriesType.CPU ) ) );
        result.setMemory( getMemoryDto( seriesMap.get( SeriesBatch.SeriesType.MEMORY ) ) );
        result.setDisk( getDiskDto( seriesMap.get( SeriesBatch.SeriesType.DISK ) ) );

        return result;
    }
}