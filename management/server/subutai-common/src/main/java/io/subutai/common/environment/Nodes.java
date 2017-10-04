package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.hub.share.quota.ContainerQuota;


public class Nodes
{
    @JsonProperty( value = "nodes" )
    private Set<Node> nodes;

    @JsonProperty( value = "quotas" )
    private Map<String, ContainerQuota> quotas;

    @JsonProperty( value = "environmentId" )
    private String environmentId;


    public Nodes()
    {
    }


    public Nodes( final Set<Node> nodes )
    {
        this.nodes = nodes;
    }


    public Nodes( final String environmentId, final Set<Node> nodes, final Map<String, ContainerQuota> quotas )
    {
        this.environmentId = environmentId;
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


    public String getEnvironmentId()
    {
        return environmentId;
    }
}
