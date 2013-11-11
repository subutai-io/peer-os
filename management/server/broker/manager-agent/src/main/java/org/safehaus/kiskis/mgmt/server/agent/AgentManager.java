package org.safehaus.kiskis.mgmt.server.agent;

import java.util.*;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

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
    public synchronized boolean registerAgent(Agent agent) {
        boolean added = registeredAgents.add(agent);
        //persistenceAgent.saveAgent(agent);

        /*Request request = new Request();
        request.setType(RequestType.REGISTRATION_REQUEST_DONE);
        request.setUuid(agent.getUuid());
        Command command = new Command(request);
        commandManager.executeCommand(command);*/

        if(added){
            notifyModules();
        }
        return false;
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
