package io.subutai.common.metric;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.dto.metrics.CpuDto;
import io.subutai.bazaar.share.dto.metrics.DiskDto;
import io.subutai.bazaar.share.dto.metrics.HostMetricsDto;
import io.subutai.bazaar.share.dto.metrics.MemoryDto;
import io.subutai.bazaar.share.dto.metrics.NetDto;


/**
 * Historical metrics
 */
@JsonIgnoreProperties( ignoreUnknown = true )
public class HistoricalMetrics
{
    private static final Logger LOG = LoggerFactory.getLogger( HistoricalMetrics.class );

    private static String CONTAINER_PARTITION = "total";
    @JsonProperty( "startTime" )
    private Date startTime;

    @JsonProperty( "endTime" )
    private Date endTime;

    @JsonProperty( "Metrics" )
    private List<SeriesBatch> metrics = new ArrayList<>();
    private Map<SeriesBatch.SeriesType, List<Series>> seriesMap = new HashMap<>();


    public HistoricalMetrics()
    {
    }


    public HistoricalMetrics( @JsonProperty( "startTime" ) final Date startTime,
                              @JsonProperty( "endTime" ) final Date endTime,
                              @JsonProperty( "Metrics" ) final List<SeriesBatch> metrics )
    {
        this.startTime = startTime;
        this.endTime = endTime;
        this.metrics = metrics;
    }


    public HistoricalMetrics( final Date startTime, final Date endTime )
    {
        this.startTime = startTime;
        this.endTime = endTime;
    }


    public Date getStartTime()
    {
        return startTime;
    }


    public void setStartTime( final Date startTime )
    {
        this.startTime = startTime;
    }


    public Date getEndTime()
    {
        return endTime;
    }


    public void setEndTime( final Date endTime )
    {
        this.endTime = endTime;
    }


    @JsonIgnore
    public List<SeriesBatch> getMetrics()
    {
        return metrics;
    }


    @JsonIgnore
    HostMetricsDto.HostType getHostType()
    {
        HostMetricsDto.HostType result = HostMetricsDto.HostType.UNKNOWN;
        if ( metrics != null && !metrics.isEmpty() && ( metrics.get( 0 ).getSeries() != null ) && !( metrics.get( 0 )
                                                                                                            .getSeries()
                                                                                                            .isEmpty() ) )
        {
            String name = metrics.get( 0 ).getSeries().get( 0 ).getName();
            if ( name != null )
            {
                if ( name.startsWith( "host_" ) )
                {
                    result = HostMetricsDto.HostType.RESOURCE_HOST;
                }
                else
                {
                    if ( name.startsWith( "lxc_" ) )
                    {
                        result = HostMetricsDto.HostType.CONTAINER_HOST;
                    }
                }
            }
        }

        return result;
    }


    @JsonIgnore
    List<Series> getSeriesByType( final SeriesBatch.SeriesType type )
    {
        if ( type == null )
        {
            throw new IllegalArgumentException( "Series type could not be null." );
        }

        List<Series> result = new ArrayList<>();
        final HostMetricsDto.HostType hostType = getHostType();

        if ( metrics != null )
        {
            for ( SeriesBatch batch : metrics )
            {
                result.addAll( batch.getSeriesByName( type.getName( hostType ) ) );
            }
        }

        return result;
    }


    public Map<SeriesBatch.SeriesType, List<Series>> getSeriesMap()
    {
        splitSeries();
        return seriesMap;
    }


    private void splitSeries()
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
        if ( getHostType() == HostMetricsDto.HostType.RESOURCE_HOST )
        {
            cpuDto.setIdle( SeriesHelper.getAvg( series, new Tag( "type", "idle" ) ) );
            cpuDto.setIowait( SeriesHelper.getAvg( series, new Tag( "type", "iowait" ) ) );
            cpuDto.setNice( SeriesHelper.getAvg( series, new Tag( "type", "nice" ) ) );
        }
        return cpuDto;
    }


    @JsonIgnore
    private MemoryDto getMemoryDto( final List<Series> series )
    {
        MemoryDto memoryDto = new MemoryDto();
        if ( getHostType() == HostMetricsDto.HostType.RESOURCE_HOST )
        {
            memoryDto.setBuffers( SeriesHelper.getAvg( series, new Tag( "type", "buffers" ) ) );
            memoryDto.setActive( SeriesHelper.getAvg( series, new Tag( "type", "active" ) ) );
            memoryDto.setCached( SeriesHelper.getAvg( series, new Tag( "type", "cached" ) ) );
            memoryDto.setMemFree( SeriesHelper.getAvg( series, new Tag( "type", "memfree" ) ) );
        }
        else
        {
            memoryDto.setCached( SeriesHelper.getAvg( series, new Tag( "type", "cache" ) ) );
            memoryDto.setRss( SeriesHelper.getAvg( series, new Tag( "type", "rss" ) ) );
        }
        return memoryDto;
    }


    @JsonIgnore
    private Map<String, DiskDto> getDiskDto( final List<Series> series )
    {
        Map<String, DiskDto> result = new HashMap<>();

        if ( getHostType() == HostMetricsDto.HostType.RESOURCE_HOST )
        {
            for ( String mount : SeriesHelper.getTagValues( series, "mount" ) )
            {
                DiskDto dto = new DiskDto();
                dto.setAvailable(
                        SeriesHelper.getAvg( series, new Tag( "type", "available" ), new Tag( "mount", mount ) ) );
                dto.setTotal( SeriesHelper.getAvg( series, new Tag( "type", "total" ), new Tag( "mount", mount ) ) );
                dto.setUsed( SeriesHelper.getAvg( series, new Tag( "type", "used" ), new Tag( "mount", mount ) ) );
                result.put( mount, dto );
            }
        }
        else
        {
            DiskDto dto = new DiskDto();
            dto.setUsed( SeriesHelper.getAvg( series, new Tag( "type", "used" ), new Tag( "mount", "total" ) ) );
            result.put( CONTAINER_PARTITION, dto );
        }

        return result;
    }


    public double getContainerDiskUsed()
    {
        if ( getHostType() == HostMetricsDto.HostType.CONTAINER_HOST )
        {
            try
            {
                return getDiskDto( seriesMap.get( SeriesBatch.SeriesType.DISK ) ).get( CONTAINER_PARTITION ).getUsed();
            }
            catch ( Exception e )
            {
                LOG.warn( "Error getting disk metrics: {}", e.getMessage() );
            }
        }
        return 0D;
    }


    @JsonIgnore
    private Map<String, NetDto> getNetDto( final List<Series> series )
    {
        Map<String, NetDto> result = new HashMap<>();

        if ( getHostType() == HostMetricsDto.HostType.RESOURCE_HOST )
        {
            for ( String iface : SeriesHelper.getTagValues( series, "iface" ) )
            {
                double in = SeriesHelper.getAvg( series, new Tag( "iface", iface ), new Tag( "type", "in" ) );
                double out = SeriesHelper.getAvg( series, new Tag( "iface", iface ), new Tag( "type", "out" ) );
                NetDto dto = new NetDto( iface, in, out );
                result.put( iface, dto );
            }
        }
        else
        {
            double in = SeriesHelper.getAvg( series, new Tag( "type", "in" ) );
            double out = SeriesHelper.getAvg( series, new Tag( "type", "out" ) );
            NetDto dto = new NetDto( "eth0", in, out );
            result.put( "eth0", dto );
        }

        return result;
    }


    @JsonIgnore
    public HostMetricsDto getHostMetrics()
    {
        HostMetricsDto result = new HostMetricsDto();

        try
        {
            HostMetricsDto.HostType hostType = getHostType();
            result.setType( hostType );
            splitSeries();

            result.setCpu( getCpuDto( seriesMap.get( SeriesBatch.SeriesType.CPU ) ) );
            result.setMemory( getMemoryDto( seriesMap.get( SeriesBatch.SeriesType.MEMORY ) ) );
            result.setDisk( getDiskDto( seriesMap.get( SeriesBatch.SeriesType.DISK ) ) );
            result.setNet( getNetDto( seriesMap.get( SeriesBatch.SeriesType.NET ) ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error parsing metric: {}", e.getMessage() );
        }

        return result;
    }


    public static boolean isZeroMetric( HostMetricsDto hostMetricsDto )
    {
        if ( hostMetricsDto.getType() == HostMetricsDto.HostType.CONTAINER_HOST )
        {
            //check memory
            boolean isZero = hostMetricsDto.getCpu().getSystem() == 0D;
            isZero &= hostMetricsDto.getCpu().getUser() == 0D;
            //check cpu
            isZero &= hostMetricsDto.getMemory().getCached() == 0D;
            isZero &= hostMetricsDto.getMemory().getRss() == 0D;
            //check disk
            isZero &= hostMetricsDto.getDisk().get( CONTAINER_PARTITION ) == null
                    || hostMetricsDto.getDisk().get( CONTAINER_PARTITION ).getUsed() == 0D;
            //check network
            isZero &= hostMetricsDto.getNet().get( "eth0" ) == null
                    || hostMetricsDto.getNet().get( "eth0" ).getIn() == 0D;
            isZero &= hostMetricsDto.getNet().get( "eth0" ) == null
                    || hostMetricsDto.getNet().get( "eth0" ).getOut() == 0D;

            return isZero;
        }
        else
        {
            //for near future we don't skip sending RH metrics
            return false;
        }
    }
}