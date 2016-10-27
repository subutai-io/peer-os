package io.subutai.common.metric;


import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

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
    Collection<ResourceHostMetric> resources = new HashSet();


    public Collection<ResourceHostMetric> getResources()
    {
        return resources;
    }


    public void addMetric( final ResourceHostMetric resourceHostMetric )
    {
        this.resources.add( resourceHostMetric );
    }


    public ResourceHostMetric get( String hostId )
    {
        ResourceHostMetric result = null;

        for ( Iterator<ResourceHostMetric> i = resources.iterator(); result == null && i.hasNext(); )
        {
            ResourceHostMetric r = i.next();
            if ( r.getHostInfo().getId().equals( hostId ) )
            {
                result = r;
            }
        }
        return result;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return CollectionUtils.isEmpty( resources );
    }
}
