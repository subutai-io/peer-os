package org.safehaus.kiskis.mgmt.api.flume;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class Config {

    public static final String PRODUCT_KEY = "Flume v2";
    private String clusterName = "";
    private int numberOfNodes = 3;
    private Set<Agent> nodes;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", numberOfNodes=" + numberOfNodes + ", nodes=" + nodes + '}';
    }

}
