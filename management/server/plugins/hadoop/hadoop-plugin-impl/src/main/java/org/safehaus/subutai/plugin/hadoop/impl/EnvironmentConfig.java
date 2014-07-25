package org.safehaus.subutai.plugin.hadoop.impl;

import com.google.common.collect.Sets;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.helper.*;
import org.safehaus.subutai.plugin.hadoop.api.HadoopClusterConfig;
import org.safehaus.subutai.plugin.hadoop.api.NodeType;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by daralbaev on 7/25/14.
 */
public class EnvironmentConfig {
	public final static int NUMBER_OF_MASTER_NODES = 3;

	private HadoopClusterConfig config;
	private EnvironmentBlueprint blueprint;
	private NodeGroup masterNodes;

	public EnvironmentConfig(HadoopClusterConfig config) {
		this.config = config;
		blueprint = new EnvironmentBlueprint();
		blueprint.setName(config.getClusterName());
		setMasterNodes();
		blueprint.setNodeGroups(Sets.newHashSet(masterNodes));
	}

	private void setMasterNodes() {
		masterNodes = new NodeGroup();
		masterNodes.setName(config.getClusterName() + "_MasterNodes");
		masterNodes.setNumberOfNodes(EnvironmentConfig.NUMBER_OF_MASTER_NODES + config.getCountOfSlaveNodes());
		masterNodes.setTemplateName(config.getTemplateName());
		masterNodes.setPlacementStrategy(getNodePlacementStrategyByNodeType(NodeType.DEFAULT_NODE));
		masterNodes.setLinkHosts(true);
		masterNodes.setExchangeSshKeys(true);

		Set<Agent> physicalAgents = HadoopImpl.getAgentManager().getPhysicalAgents();
		Set<String> stringAgents = new HashSet<>();
		for (Agent agent : physicalAgents) {
			stringAgents.add(agent.getHostname());
		}
		masterNodes.setPhysicalNodes(stringAgents);
	}

	public static PlacementStrategyENUM getNodePlacementStrategyByNodeType(NodeType nodeType) {
		switch (nodeType) {
			case MASTER_NODE:
				return PlacementStrategyENUM.MORE_RAM;
			case SLAVE_NODE:
				return PlacementStrategyENUM.MORE_HDD;
			default:
				return PlacementStrategyENUM.ROUND_ROBIN;
		}
	}

	public HadoopClusterConfig setup() throws EnvironmentBuildException {
		Environment environment = HadoopImpl.getEnvironmentManager().buildEnvironmentAndReturn(blueprint);
		setMasterNodes(environment);
//		setSlaveNodes(environment);

		return config;
	}

	private void setMasterNodes(Environment environment) {
		Set<Node> nodes = environment.getNodes();
		if (nodes != null && nodes.size() >= 2) {

			Node[] arr = nodes.toArray(new Node[nodes.size()]);
			System.out.println(arr);

			/*if (arr[0].getTemplate().getProducts().contains(config.getTemplateName())) {
				config.setNameNode(arr[0].getAgent());
			}

			if (arr[1].getTemplate().getProducts().contains(config.getTemplateName())) {
				config.setJobTracker(arr[1].getAgent());
			}

			if (arr[0].getTemplate().getProducts().contains(config.getTemplateName())) {
				config.setSecondaryNameNode(arr[2].getAgent());
			}*/
		}
	}

	private void setSlaveNodes(Environment environment) {
		Set<Node> nodes = environment.getNodes();
		if (nodes != null && nodes.size() > 2) {
			Set<Node> slaveNodes = Sets.difference(nodes, Sets.newHashSet(config.getNameNode(), config.getSecondaryNameNode(), config.getJobTracker()));
			for (Node node : slaveNodes) {
				if (node.getTemplate().getProducts().contains(config.getTemplateName())) {
					config.getDataNodes().add(node.getAgent());
					config.getTaskTrackers().add(node.getAgent());
				}
			}
		}
	}
}
