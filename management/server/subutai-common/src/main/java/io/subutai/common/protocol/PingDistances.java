package io.subutai.common.protocol;


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;


/**
 * Ping distances class
 */
public class PingDistances
{
    @JsonProperty( "distances" )
    private Map<String, PingDistance> distanceMap = new HashMap<>();


    public PingDistances( final Map<String, PingDistance> distanceMap )
    {
        this.distanceMap = distanceMap;
    }


    public PingDistances()
    {

    }


    public void add( PingDistance distance )
    {
        if ( distance == null )
        {
            throw new IllegalArgumentException( "Ping distance could not be null." );
        }

        distanceMap.put( distance.getSourceIp() + "-" + distance.getTargetIp(), distance );
    }


    public void addAll( Collection<PingDistance> distances )
    {
        if ( distances == null )
        {
            throw new IllegalArgumentException( "Ping distances could not be null." );
        }

        for ( PingDistance distance : distances )
        {
            distanceMap.put( distance.getSourceIp() + "-" + distance.getTargetIp(), distance );
        }
    }


    @JsonIgnore
    public Collection<PingDistance> getAll()
    {
        return distanceMap.values();
    }


    @JsonIgnore
    public PingDistance get( final String source, final String target )
    {
        if ( distanceMap == null )
        {
            return null;
        }

        PingDistance result = distanceMap.get( source + "-" + target );
        if ( result != null )
        {
            return result;
        }
        else
        {
            return distanceMap.get( target + "-" + source );
        }
    }
}
