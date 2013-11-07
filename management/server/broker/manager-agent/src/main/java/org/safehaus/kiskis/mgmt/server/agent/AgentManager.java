package org.safehaus.kiskis.mgmt.server.agent;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/7/13
 * Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface {
    private PersistenceAgentInterface persistenceAgent;
    private CommandManagerInterface commandManager;

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return null;
    }

    @Override
    public boolean registerAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " registerAgent called");
        System.out.println(agent.toString());
        return false;
    }

    public void setPersistenceAgentService(PersistenceAgentInterface persistenceAgent) {
        this.persistenceAgent = persistenceAgent;
        System.out.println(this.getClass().getName() + " PersistenceAgentInterface initialized");
    }

    public void setCommandManagerService(CommandManagerInterface commandManager) {
        this.commandManager = commandManager;
        System.out.println(this.getClass().getName() + " CommandManagerInterface initialized");
    }
}
