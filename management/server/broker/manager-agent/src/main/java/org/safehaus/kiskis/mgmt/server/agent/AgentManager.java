package org.safehaus.kiskis.mgmt.server.agent;

//import org.osgi.framework.BundleContext;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 11:11 PM
 */
public class AgentManager implements AgentManagerInterface, BrokerListener {

    private static final Logger LOG = Logger.getLogger(AgentManager.class.getName());
//    private BundleContext context;
    private PersistenceInterface persistenceAgent;
    private CommandManagerInterface commandManager;
    private CommandTransportInterface commandTransportInterface;
    private final Set<Agent> registeredAgents;
    private final ArrayList<AgentListener> listeners = new ArrayList<AgentListener>();
    private ExecutorService executorService;
    private int heartbeatTimeoutSec;

    public AgentManager() {
        registeredAgents = new HashSet<Agent>();
    }

    @Override
    public Set<Agent> getRegisteredAgents() {
        return Collections.unmodifiableSet(registeredAgents);
    }

    @Override
    public synchronized void getCommand(Response response) {
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                saveAgent(response);
                break;
            }
            case HEARTBEAT_RESPONSE: {
                updateAgent(response);
            }
            default: {
                break;
            }
        }
    }

    private void saveAgent(Response response) {
        Agent agent = new Agent();
        agent.setUuid(response.getUuid());
        agent.setHostname(response.getHostname());
        agent.setMacAddress(response.getMacAddress());
        if (response.isIsLxc() == null) {
            agent.setIsLXC(false);
        } else {
            agent.setIsLXC(response.isIsLxc());
        }
        agent.setListIP(response.getIps());

        if (persistenceAgent.saveAgent(agent)) {

            Request request = new Request();
            request.setType(RequestType.REGISTRATION_REQUEST_DONE);
            request.setUuid(agent.getUuid());
            request.setSource(response.getSource());
            request.setStdErr(OutputRedirection.NO);
            request.setStdOut(OutputRedirection.NO);
            Command command = new Command(request);
            commandManager.executeCommand(command);

            if (registeredAgents.add(agent)) {
                notifyModules();
            }
            System.out.println(agent + "\nAgent is registered");
        } else {
            System.out.println(agent + "\nError registering agent");
        }
    }

    private void updateAgent(Response response) {
        Agent agent = new Agent();
        agent.setUuid(response.getUuid());
        agent.setHostname(response.getHostname());
        agent.setMacAddress(response.getMacAddress());
        if (response.isIsLxc() == null) {
            agent.setIsLXC(false);
        } else {
            agent.setIsLXC(response.isIsLxc());
        }
        agent.setListIP(response.getIps());

        if (persistenceAgent.updateAgent(agent)) {
            System.out.println(agent + "\nAgent is updated");
        } else {
            System.out.println(agent + "\nError updating agent");
        }
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

    public void init() {
        try {
            if (commandTransportInterface != null) {
                commandTransportInterface.addListener(this);
                executorService = Executors.newSingleThreadExecutor();
                executorService.execute(new AgentHeartBeat(this, commandTransportInterface, heartbeatTimeoutSec));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            if (commandTransportInterface != null) {
                commandTransportInterface.removeListener(this);
            }
            executorService.shutdownNow();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    public void setPersistenceAgentService(PersistenceInterface persistenceAgent) {
        this.persistenceAgent = persistenceAgent;
    }

    public void setCommandManagerService(CommandManagerInterface commandManager) {
        this.commandManager = commandManager;

    }

    public void setCommandTransportInterface(CommandTransportInterface commandTransportInterface) {
        this.commandTransportInterface = commandTransportInterface;
    }

//    public void setContext(BundleContext context) {
//        this.context = context;
//    }

//    private CommandTransportInterface getCommandTransport() {
//        if (context != null) {
//            ServiceReference reference = context
//                    .getServiceReference(CommandTransportInterface.class.getName());
//            if (reference != null) {
//                return (CommandTransportInterface) context.getService(reference);
//            }
//        }
//
//        return null;
//    }
    public void setHeartbeatTimeoutSec(int heartbeatTimeoutSec) {
        this.heartbeatTimeoutSec = heartbeatTimeoutSec;
    }
}
