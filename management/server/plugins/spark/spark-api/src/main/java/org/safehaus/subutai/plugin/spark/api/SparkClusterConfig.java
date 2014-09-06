package org.safehaus.subutai.plugin.spark.api;

import java.util.HashSet;
import java.util.Set;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.ConfigBase;

public class SparkClusterConfig implements ConfigBase {

    public static final String PRODUCT_KEY = "Spark";
    public static final String TEMPLATE_NAME = "spark";

    private String clusterName = "";
    private SetupType setupType;
    private Agent masterNode;
    private Set<Agent> slaves = new HashSet<>();

    public Agent getMasterNode() {
        return masterNode;
    }

    public void setMasterNode(Agent masterNode) {
        this.masterNode = masterNode;
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public SetupType getSetupType() {
        return setupType;
    }

    public void setSetupType(SetupType setupType) {
        this.setupType = setupType;
    }

    @Override
    public String getProductName() {
        return PRODUCT_KEY;
    }

    public Set<Agent> getSlaveNodes() {
        return slaves;
    }

    public void setSlaveNodes(Set<Agent> slaves) {
        this.slaves = slaves;
    }

    public Set<Agent> getAllNodes() {
        Set<Agent> allNodes = new HashSet<>();
        if(slaves != null) allNodes.addAll(slaves);
        if(masterNode != null) allNodes.add(masterNode);

        return allNodes;
    }

    @Override
    public String toString() {
        return "Config{" + "clusterName=" + clusterName + ", masterNode=" + masterNode + ", slaves=" + slaves + '}';
    }

}
