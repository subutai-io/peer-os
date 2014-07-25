package org.safehaus.subutai.api.manager.helper;


import org.safehaus.subutai.api.templateregistry.Template;
import org.safehaus.subutai.shared.protocol.Agent;


/**
 * Created by bahadyr on 7/24/14.
 */
public class Node {

	private Agent agent;
	private Template template;


	public Node(final Agent agent, final Template template) {
		this.agent = agent;
		this.template = template;
	}


	public Agent getAgent() {
		return agent;
	}


	public Template getTemplate() {
		return template;
	}

	@Override
	public String toString() {
		return "Node{" +
				"agent=" + agent.toString() +
				", template=" + template.toString() +
				'}';
	}
}
