package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerQuota;


public class Nodes
{
    @JsonProperty( value = "nodes" )
    private Set<Node> nodes = new HashSet<>();

    @JsonProperty( value = "quotas" )
    Map<String, ContainerQuota> quotas;


    public Nodes( @JsonProperty( value = "nodes" ) final Set<Node> nodes )
    {
        this.nodes = nodes;
    }


    public Nodes( @JsonProperty( value = "nodes" ) final Set<Node> nodes,
                  @JsonProperty( value = "quotas" ) final Map<String, ContainerQuota> quotas )
    {
        this.nodes = nodes;
        this.quotas = quotas;
    }


    public Map<String, ContainerQuota> getQuotas()
    {
        return quotas;
    }


    public Set<Node> getNodes()
    {
        return nodes;
    }
}
