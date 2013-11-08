package org.safehaus.kiskis.mgmt.server.agent;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/7/13
 * Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface {
    private PersistenceAgentInterface persistenceAgent;
    private CommandManagerInterface commandManager;
    List<Agent> registeredAgents;

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return registeredAgents;
    }

    @Override
    public boolean registerAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " registerAgent called");
        registeredAgents.add(agent);
        Response response = new Response();
        response.setType(ResponseType.REGISTRATION_REQUEST_DONE);
        response.setUuid(agent.getUuid());
        Command command = new Command(response);
        commandManager.saveCommand(command);
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
