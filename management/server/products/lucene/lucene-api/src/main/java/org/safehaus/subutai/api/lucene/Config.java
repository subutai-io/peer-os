package org.safehaus.subutai.api.lucene;


import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;


public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Lucene";

    private String clusterName = "";
    private String luceneClusterName = "";

    private Set<Agent> nodes = new HashSet<>();


    public String getLuceneClusterName() {
        return luceneClusterName;
    }


    public void setLuceneClusterName( final String luceneClusterName ) {
        this.luceneClusterName = luceneClusterName;
    }


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
        return "Config{" +
                "clusterName='" + clusterName + '\'' +
                ", luceneClusterName='" + luceneClusterName + '\'' +
                ", nodes=" + nodes +
                '}';
    }


    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }
}
