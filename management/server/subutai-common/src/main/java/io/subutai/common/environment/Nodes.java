package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.quota.ContainerQuota;


public class Nodes
{
    @JsonProperty( value = "nodes" )
    private Set<Node> nodes;

    @JsonProperty( value = "quotas" )
    private Map<String, ContainerQuota> quotas;


    public Nodes()
    {
    }


    public Nodes( final Set<Node> nodes )
    {
        this.nodes = nodes;
    }


    public Nodes( final Set<Node> nodes, final Map<String, ContainerQuota> quotas )
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
