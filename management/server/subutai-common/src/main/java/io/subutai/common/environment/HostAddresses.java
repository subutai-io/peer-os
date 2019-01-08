package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.gson.annotations.Expose;

import io.subutai.common.peer.ContainerHost;
import io.subutai.common.util.CollectionUtil;


public class HostAddresses
{
    @Expose
    @JsonProperty( "hostAddresses" )
    private Map<String, String> hostAddresses;


    public HostAddresses( Set<ContainerHost> containerHostSet )
    {
        Preconditions.checkArgument( !CollectionUtil.isCollectionEmpty( containerHostSet ) );

        hostAddresses = Maps.newHashMap();

        for ( ContainerHost containerHost : containerHostSet )
        {
            hostAddresses.put( containerHost.getHostname(), containerHost.getIp() );
        }
    }


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
