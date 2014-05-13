package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-agents", description = "get the list of agents")
public class GetAgentsCommand extends OsgiCommandSupport {

    private AgentManager agentManager;

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    protected Object doExecute() {
        Set<Agent> agentList = agentManager.getAgents();
        StringBuilder sb = new StringBuilder();

        for(Agent agent : agentList) {
            sb.append(agent.getHostname());
        }

        System.out.println(sb.toString());

        System.out.println("get-agents command executed");
        return null;
    }
}
