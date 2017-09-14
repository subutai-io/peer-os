package io.subutai.common.environment;


import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;


public class Nodes
{
    @JsonProperty( value = "nodes" )
    private Set<Node> nodes = new HashSet<>();


    public Nodes( @JsonProperty( value = "nodes" ) final Set<Node> nodes )
    {
        this.nodes = nodes;
    }


    public Set<Node> getNodes()
    {
        return nodes;
    }
}
