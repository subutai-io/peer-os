package org.safehaus.subutai.api.mahout;

import java.util.HashSet;
import java.util.Set;

import org.safehaus.subutai.shared.protocol.Agent;
import org.safehaus.subutai.shared.protocol.ConfigBase;


public class MahoutConfig implements ConfigBase {

	public static final String PRODUCT_KEY = "Mahout";
	public static final String PRODUCT_NAME = "Mahout";
	private String clusterName = "";
    private String templateName = PRODUCT_NAME;

	private Set<Agent> nodes = new HashSet();

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


    public String getTemplateName() {
        return templateName;
    }


    public void setTemplateName( final String templateName ) {
        this.templateName = templateName;
    }


    @Override
	public String toString() {
		return "Config{" + "clusterName=" + clusterName + ", nodes=" + nodes + '}';
	}

}
