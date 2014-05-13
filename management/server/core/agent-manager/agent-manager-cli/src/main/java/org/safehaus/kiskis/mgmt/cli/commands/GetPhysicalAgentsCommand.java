package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-physical-agents", description = "get the list of physical-agents")
public class GetPhysicalAgentsCommand extends OsgiCommandSupport {

    private AgentManager agentManager;

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    protected Object doExecute() {
        System.out.println("get-physical-agents command executed");
        return null;
    }
}
