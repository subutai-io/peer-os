package org.safehaus.subutai.plugin.sqoop.api;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

public class SqoopConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Sqoop2";
    public static final String TEMPLATE_NAME = "hadoopsqoop";
    private String clusterName;
    private int nodesCount;
    private Set<Agent> nodes = new HashSet();

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public int getNodesCount() {
        return nodesCount;
    }

    public void setNodesCount(int nodesCount) {
        this.nodesCount = nodesCount;
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
