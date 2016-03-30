package io.subutai.common.environment;


import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;


public class HostAddresses
{
    @Expose
    @JsonProperty( "hostAddresses" )
    private Map<String, String> hostAddresses = Maps.newHashMap();


    public HostAddresses( @JsonProperty( "hostAddresses" ) final Map<String, String> hostAddresses )
    {
        this.hostAddresses = hostAddresses;
    }


    public HostAddresses()
    {
    }


    public void addHostAddress( String hostname, String ip )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( hostname ), "Invalid hostname" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( ip ), "Invalid ip" );

        hostAddresses.put( hostname, ip );
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
