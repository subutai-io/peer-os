package io.subutai.common.environment;


import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.subutai.bazaar.share.quota.ContainerQuota;


public class Nodes
{
    @JsonProperty( value = "nodes" )
    private Set<Node> newNodes;
    @JsonProperty( value = "removedContainers" )
    private Set<String> removedContainers;

    @JsonProperty( value = "quotas" )
    private Map<String, ContainerQuota> quotas;


    public Nodes()
    {
    }


    public Nodes( final Set<Node> newNodes )
    {
        this.newNodes = newNodes;
    }


    public Nodes( final Set<Node> newNodes, final Set<String> removedContainers,
                  final Map<String, ContainerQuota> quotas )
    {
        this.newNodes = newNodes;
        this.quotas = quotas;
        this.removedContainers = removedContainers;
    }


    public Map<String, ContainerQuota> getQuotas()
    {
        return quotas;
    }


    public Set<Node> getNewNodes()
    {
        return newNodes;
    }


    public Set<String> getRemovedContainers()
    {
        return removedContainers;
    }
}
