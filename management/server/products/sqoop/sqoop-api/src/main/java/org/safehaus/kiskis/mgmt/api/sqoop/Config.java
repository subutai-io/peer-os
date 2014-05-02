package org.safehaus.kiskis.mgmt.api.sqoop;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.ConfigBase;

import java.util.Set;

public class Config implements ConfigBase {

    public static final String PRODUCT_KEY = "Sqoop";
    String clusterName;
    Set<Agent> nodes;

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }

    public Set<Agent> getNodes() {
        return nodes;
    }

    public void setNodes(Set<Agent> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", nodes="
                + (nodes != null ? nodes.size() : 0) + '}';
    }

}
