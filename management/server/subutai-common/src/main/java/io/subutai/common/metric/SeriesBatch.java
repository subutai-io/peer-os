package io.subutai.common.metric;


import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.dto.metrics.HostMetricsDto;


/**
 * Historical metrics series batch
 */
public class SeriesBatch
{
    public enum SeriesType
    {
        CPU( "host_cpu", "lxc_cpu" ), MEMORY( "host_memory", "lxc_memory" ), DISK( "host_disk", "lxc_disk" ),
        NET( "host_net", "lxc_net" );
        private String rhName;
        private String chName;


        SeriesType( final String rhName, final String chName )
        {
            this.rhName = rhName;
            this.chName = chName;
        }


        public String getName( final HostMetricsDto.HostType hostType )
        {
            return hostType == HostMetricsDto.HostType.RESOURCE_HOST ? rhName : chName;
        }
    }


    @JsonProperty( "Series" )
    List<Series> series = new ArrayList<>();

    @JsonProperty( "Messages" )
    String messages;


    public List<Series> getSeries()
    {
        return series;
    }


    public String getMessages()
    {
        return messages;
    }


    @JsonIgnore
    public List<Series> getSeriesByName( final String name )
    {
        if ( name == null )
        {
            throw new IllegalArgumentException( "Series name could not be null." );
        }

        List<Series> result = new ArrayList<>();

        for ( Series s : series )
        {
            if ( name.equalsIgnoreCase( s.getName() ) )
            {
                result.add( s );
            }
        }
        return result;
    }
}
