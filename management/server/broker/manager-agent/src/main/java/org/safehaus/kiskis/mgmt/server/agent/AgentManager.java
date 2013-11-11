package org.safehaus.kiskis.mgmt.server.agent;

import java.util.ArrayList;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.PersistenceAgentInterface;

import java.util.Collections;
import java.util.List;

import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface {

    private PersistenceAgentInterface persistenceAgent;
    private CommandManagerInterface commandManager;
    List<Agent> registeredAgents;
    private ArrayList<AgentInterface> modules = new ArrayList<AgentInterface>();
    private ArrayList<AgentListener> listeners = new ArrayList<AgentListener>();

    public AgentManager() {
        registeredAgents = new ArrayList<Agent>();
    }

    @Override
    public List<Agent> getAgentList() {
        System.out.println(this.getClass().getName() + " getAgentList called");
        return registeredAgents;
    }

    @Override
    public synchronized boolean registerAgent(Agent agent) {
        System.out.println(this.getClass().getName() + " registerAgent called");
        registeredAgents.add(agent);

        Request request=  new Request();
        request.setType(RequestType.REGISTRATION_REQUEST_DONE);
        request.setUuid(agent.getUuid());
        Command command = new Command(request);
        commandManager.executeCommand(command);
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void registerAgentInterface(AgentInterface module) {
        modules.add(module);
        for (AgentListener listener : (ArrayList<AgentListener>) listeners.clone()) {
            if(listener != null){
                listener.agentRegistered(this, module);
            }
        }
        System.out.println("New agent listener registered");
    }

    @Override
    public List<AgentInterface> getModules() {
        return Collections.unmodifiableList(modules);
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
