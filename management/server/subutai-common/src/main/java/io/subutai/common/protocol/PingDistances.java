package io.subutai.common.protocol;


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


    @JsonIgnore
    public PingDistance getDistance( final String source, final String target )
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
