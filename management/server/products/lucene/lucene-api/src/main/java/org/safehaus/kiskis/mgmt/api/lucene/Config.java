package org.safehaus.kiskis.mgmt.api.lucene;


import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.HashSet;
import java.util.Set;


public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Lucene";

    private String clusterName = "";

    private Set<Agent> nodes = new HashSet<>();


    @Override
    public String getClusterName() {
        return clusterName;
    }


    public Config setClusterName( String clusterName ) {
        this.clusterName = clusterName;
        return this;
    }


    public Set<Agent> getNodes() {
        return nodes;
    }


    public void setNodes( Set<Agent> nodes ) {
        this.nodes = nodes;
    }


    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }
}
