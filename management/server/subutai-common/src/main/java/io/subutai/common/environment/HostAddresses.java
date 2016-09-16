package io.subutai.common.environment;


import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;


public class HostAddresses
{
    @Expose
    @JsonProperty( "hostAddresses" )
    private Map<String, String> hostAddresses = Maps.newHashMap();


    public HostAddresses( @JsonProperty( "hostAddresses" ) final Map<String, String> hostAddresses )
    {
        Preconditions.checkNotNull( hostAddresses );

        this.hostAddresses = hostAddresses;
    }


    /**
     * Returns map where key is hostname and value is ip
     */
    public Map<String, String> getHostAddresses()
    {
        return hostAddresses;
    }


    @JsonIgnore
    public boolean isEmpty()
    {
        return hostAddresses == null || hostAddresses.isEmpty();
    }
}
