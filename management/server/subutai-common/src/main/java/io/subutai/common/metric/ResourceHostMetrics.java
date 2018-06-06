package io.subutai.common.metric;


import java.util.HashSet;
import java.util.Set;

import org.apache.cxf.common.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.Expose;


/**
 * Collection of resource hosts metrics
 */
public class ResourceHostMetrics
{
    @Expose
    @JsonProperty( "resources" )
    Set<ResourceHostMetric> resources = new HashSet();
    @Expose
    @JsonProperty( "count" )
    int resourceHostCount;


    public Set<ResourceHostMetric> getResources()
    {
        return resources;
    }


    public void addMetric( final ResourceHostMetric resourceHostMetric )
    {
        this.resources.add( resourceHostMetric );
    }


    public ResourceHostMetric get( String hostId )
    {
        for ( ResourceHostMetric metric : resources )
        {
            if ( metric.getHostInfo().getId().equals( hostId ) )
            {
                return metric;
            }
        }

        return null;
    }


    public void setResourceHostCount( final int resourceHostCount )
    {
        this.resourceHostCount = resourceHostCount;
    }


    public int getResourceHostCount()
    {
        return resourceHostCount;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtils.isEmpty( resources );
    }
}
