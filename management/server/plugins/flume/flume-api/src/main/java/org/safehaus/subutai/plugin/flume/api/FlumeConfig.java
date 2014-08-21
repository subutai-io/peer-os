package org.safehaus.subutai.plugin.flume.api;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

public class FlumeConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Flume";
    public static final String TEMPLATE_NAME = "flume";

    private String clusterName = "";
    private String hadoopClusterName;
    private Set<Agent> nodes = new HashSet();

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public String getHadoopClusterName() {
        return hadoopClusterName;
    }

    public void setHadoopClusterName(String hadoopClusterName) {
        this.hadoopClusterName = hadoopClusterName;
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
        int nodesCount = nodes != null ? nodes.size() : 0;
        return "Config{" + "clusterName=" + clusterName + ", hadoopClusterName="
                + hadoopClusterName + ", nodes=" + nodesCount + '}';
    }

}
