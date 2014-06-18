package org.safehaus.subutai.api.storm;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;

import java.util.Set;

public class Config implements ConfigBase {

	public static final String PRODUCT_NAME = "Storm";

    private String clusterName;
    private Agent nimbus; // master node
    private Set<Agent> supervisors; // worker nodes

    public String getClusterName() {
        return clusterName;
    }

    public String getProductName() {
        return PRODUCT_NAME;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }

    public Agent getNimbus() {
        return nimbus;
    }

    public void setNimbus(Agent nimbus) {
        this.nimbus = nimbus;
    }

    public Set<Agent> getSupervisors() {
        return supervisors;
    }

    public void setSupervisors(Set<Agent> supervisors) {
        this.supervisors = supervisors;
    }

}
