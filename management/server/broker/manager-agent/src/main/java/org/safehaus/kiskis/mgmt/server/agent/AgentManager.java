package org.safehaus.kiskis.mgmt.server.agent;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface {

    private PersistenceAgentInterface persistenceAgent;
    private CommandManagerInterface commandManager;
    private Set<Agent> registeredAgents;
    private ArrayList<AgentListener> listeners = new ArrayList<AgentListener>();

    public AgentManager() {
        registeredAgents = new HashSet<Agent>();
    }

    @Override
    public Set<Agent> getRegisteredAgents() {
        return Collections.unmodifiableSet(registeredAgents);
    }

    @Override
    public synchronized void registerAgent(Response response) {
        Agent agent = getAgent(response);

        if (persistenceAgent.saveAgent(agent)) {

            Request request = new Request();
            request.setType(RequestType.REGISTRATION_REQUEST_DONE);
            request.setUuid(agent.getUuid());
            Command command = new Command(request);
            commandManager.executeCommand(command);

            if (registeredAgents.add(agent)) {
                notifyModules();
            }
            System.out.println(agent + "Agent is registered");
            System.out.println("Dynamic service lookup: " + ServiceLocator.getService(CommandManagerInterface.class));
        } else {
            System.out.println(agent + "Error registering agent");
        }
    }

    private Agent getAgent(Response response) {
        Agent agent = new Agent();
        agent.setUuid(response.getUuid());
        agent.setHostname(response.getHostname());
        agent.setMacAddress(response.getMacAddress());

        return agent;
    }

    private void notifyModules() {
        for (AgentListener ai : listeners) {
            if (ai != null) {
                System.out.println("Agents count: " + registeredAgents.size());
                System.out.println("Notify listener: " + ai.getId());
                ai.agentRegistered(getRegisteredAgents());
            } else {
                listeners.remove(ai);
            }
        }
    }

    @Override
    public synchronized void addListener(AgentListener listener) {
        listeners.add(listener);
    }

    @Override
    public synchronized void removeListener(AgentListener listener) {
        listeners.remove(listener);
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
