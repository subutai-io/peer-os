package org.safehaus.subutai.impl.container;

import org.safehaus.subutai.shared.protocol.PlacementStrategy;

import java.util.Set;
import java.util.UUID;


class NodeInfo {

	UUID envId;
	String templateName;
	Set<PlacementStrategy> strategy;
	Set<String> products;
	UUID instanceId;

	public UUID getEnvId() {
		return envId;
	}

	public void setEnvId(UUID envId) {
		this.envId = envId;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public Set<PlacementStrategy> getStrategy() {
		return strategy;
	}

	public void setStrategy(Set<PlacementStrategy> strategy) {
		this.strategy = strategy;
	}

	public Set<String> getProducts() {
		return products;
	}

	public void setProducts(Set<String> products) {
		this.products = products;
	}

	public UUID getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(UUID instanceId) {
		this.instanceId = instanceId;
	}

}
