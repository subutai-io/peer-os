package org.safehaus.kiskis.mgmt.cli.commands;

import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;


/**
 * Displays the last log entries
 */
@Command(scope = "agent", name = "get-lxc-agents-by-parent-hostname", description = "get LXC agents by parent hostname")
public class GetLxcAgentsByParentHostnameCommand extends OsgiCommandSupport {

    private AgentManager agentManager;

    public AgentManager getAgentManager() {
        return agentManager;
    }

    public void setAgentManager(AgentManager agentManager) {
        this.agentManager = agentManager;
    }

    protected Object doExecute() {
        System.out.println("get-lxc-agents-by-parent-hostname command executed");
        return null;
    }
}
