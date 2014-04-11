package org.safehaus.kiskis.mgmt.api.flume;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

public class Config {

    public static final String PRODUCT_KEY = "Flume";
    private String clusterName = "";
    private Set<Agent> nodes;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + '}';
    }

}
