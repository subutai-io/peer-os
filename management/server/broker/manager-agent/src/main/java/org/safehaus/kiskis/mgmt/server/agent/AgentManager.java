package org.safehaus.kiskis.mgmt.server.agent;

import java.util.ArrayList;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.List;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface {

    private PersistenceAgentInterface persistenceAgent;
    private CommandManagerInterface commandManager;
    List<Agent> registeredAgents;

    public AgentManager() {
        registeredAgents = new ArrayList<Agent>();
    }

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return registeredAgents;
    }

    @Override
    public boolean registerAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " registerAgent called");
        registeredAgents.add(agent);
        persistenceAgent.saveAgent(agent);

        Request request = new Request();
        request.setType(RequestType.REGISTRATION_REQUEST_DONE);
        request.setUuid(agent.getUuid());
        Command command = new Command(request);
        commandManager.executeCommand(command);

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
