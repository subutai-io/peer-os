package org.safehaus.subutai.ui.hive.query.components;

import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import org.safehaus.subutai.common.protocol.Agent;

/**
 * Created by daralbaev on 06.05.14.
 */
public class AgentContainer extends HorizontalLayout {

	private Label labelNode;
	private Agent agent;

	public AgentContainer(Agent agent, String caption) {
		setMargin(true);
		setSpacing(true);

		this.agent = agent;
		labelNode = new Label(caption);
		addComponent(labelNode);
	}

	public Agent getAgent() {
		return agent;
	}
}
