package org.safehaus.subutai.impl.manager.builder;


import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.container.ContainerManager;
import org.safehaus.subutai.api.lxcmanager.LxcCreateException;
import org.safehaus.subutai.api.manager.exception.EnvironmentBuildException;
import org.safehaus.subutai.api.manager.exception.EnvironmentInstanceDestroyException;
import org.safehaus.subutai.api.manager.helper.*;
import org.safehaus.subutai.api.templateregistry.TemplateRegistryManager;
import org.safehaus.subutai.shared.protocol.Agent;

import java.util.HashSet;
import java.util.Set;


/**
 * Created by bahadyr on 6/23/14.
 */
public class EnvironmentBuilder {

	private final TemplateRegistryManager templateRegistryManager;
	private final AgentManager agentManager;


	public EnvironmentBuilder(final TemplateRegistryManager templateRegistryManager,
	                          final AgentManager agentManager) {
		this.templateRegistryManager = templateRegistryManager;
		this.agentManager = agentManager;
	}


	//@todo Baha handle all exceptional situations like if physical agent is not connected, if template is not found etc
	public Environment build(final EnvironmentBlueprint blueprint, ContainerManager containerManager)
			throws EnvironmentBuildException {
		Environment environment = new Environment();
		environment.setName(blueprint.getName());
		for (NodeGroup nodeGroup : blueprint.getNodeGroups()) {
			PlacementStrategyENUM placementStrategy = nodeGroup.getPlacementStrategy();
			int nodeCount = nodeGroup.getNumberOfNodes();

			Set<Node> nodes = new HashSet<>();
			Set<Agent> physicalAgents = new HashSet<>();
			for (String host : nodeGroup.getPhysicalNodes()) {
				physicalAgents.add(agentManager.getAgentByHostname(host));
			}
			try {
				Set<Agent> agents = containerManager
						.clone(environment.getUuid(), nodeGroup.getTemplateName(), nodeCount, physicalAgents,
								placementStrategy);

				for (Agent agent : agents) {
					nodes.add(new Node(agent, templateRegistryManager.getTemplate(nodeGroup.getTemplateName())));
				}
			} catch (LxcCreateException ex) {
				throw new EnvironmentBuildException(ex.toString());
			}

			environment.setNodes(nodes);
		}

		return environment;
	}


	public void destroy(final Environment environment) throws EnvironmentInstanceDestroyException {
		//TODO destroy environment code goes here
		//        for ( EnvironmentNodeGroup nodeGroup : environment.getEnvironmentNodeGroups() ) {
		//            nodeGroupBuilder.destroy( nodeGroup );
		//        }
	}
}
