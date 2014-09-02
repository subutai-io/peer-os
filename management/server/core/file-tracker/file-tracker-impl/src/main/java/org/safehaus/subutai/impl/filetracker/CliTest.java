package org.safehaus.subutai.impl.filetracker;


import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.subutai.api.agentmanager.AgentManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.api.filetracker.FileTracker;
import org.safehaus.subutai.common.protocol.Agent;
import org.safehaus.subutai.common.protocol.Response;


/**
 * Needed mostly for testing FileTracker
 */
@Command (scope = "file-tracker", name = "test")
public class CliTest extends OsgiCommandSupport implements ResponseListener {

	private static final String CONFIG_POINTS[] = {
			"/etc",
			"/etc/ksks-agent"
	};

	private AgentManager agentManager;

	private FileTracker fileTracker;


	public void setAgentManager(AgentManager agentManager) {
		this.agentManager = agentManager;
	}


	public void setFileTracker(FileTracker fileTracker) {
		this.fileTracker = fileTracker;
	}


	protected Object doExecute() {

		fileTracker.addListener(this);

		Agent agent = getAgent();

		fileTracker.createConfigPoints(agent, CONFIG_POINTS);

//        fileTracker.removeConfigPoints( agent, CONFIG_POINTS );

//        fileTracker.listConfigPoints( agent );

		return null;
	}

	private Agent getAgent() {

		for (Agent agent : agentManager.getAgents()) {
			if ("management".equals(agent.getHostname())) {
				return agent;
			}
		}

		return null;
	}

	@Override
	public void onResponse(Response response) {
		System.out.println("Response: " + response);
	}

}
