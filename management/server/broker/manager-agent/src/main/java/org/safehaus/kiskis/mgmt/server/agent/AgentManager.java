package org.safehaus.kiskis.mgmt.server.agent;

//import org.osgi.framework.BundleContext;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

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
    private int heartbeatFromMin;
    private int heartbeatToMin;
    private int agentFreshnessMin;

    public AgentManager() {
        registeredAgents = new HashSet<Agent>();
    }

    @Override
    public synchronized void getCommand(Response response) {
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                updateAgent(response, true);
                break;
            }
            case HEARTBEAT_RESPONSE: {
                updateAgent(response, false);
            }
            default: {
                break;
            }
        }
    }

    private synchronized void updateAgent(Response response, boolean register) {
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
        if (agent.isIsLXC()) {
            if (agent.getHostname() != null && agent.getHostname().contains(Common.PARENT_CHILD_LXC_SEPARATOR)) {
                agent.setParentHostName(agent.getHostname().substring(0, agent.getHostname().indexOf(Common.PARENT_CHILD_LXC_SEPARATOR)));
            } else {
                agent.setParentHostName(Common.UNKNOWN_LXC_PARENT_NAME);
            }
        }

        if (persistenceAgent.saveAgent(agent)) {
            if (register) {
                Task task = new Task();
                task.setDescription("Agent registration");
                task.setTaskStatus(TaskStatus.NEW);
                task.setReqSeqNumber(0);
                commandManager.saveTask(task);
                response.setTaskUuid(task.getUuid());
                response.setRequestSequenceNumber(task.getReqSeqNumber());
                persistenceAgent.saveResponse(response);
                //
                Request request = new Request();
                request.setTaskUuid(task.getUuid());
                request.setRequestSequenceNumber(task.getReqSeqNumber());
                request.setType(RequestType.REGISTRATION_REQUEST_DONE);
                request.setUuid(agent.getUuid());
                request.setSource(response.getSource());
                request.setStdErr(OutputRedirection.NO);
                request.setStdOut(OutputRedirection.NO);
                Command command = new Command(request);
                commandManager.executeCommand(command);
                //
                task.setTaskStatus(TaskStatus.SUCCESS);
                persistenceAgent.saveTask(task);
                //
                if (registeredAgents.add(agent)) {
                    notifyModules();
                }
            }
            System.out.println(agent + String.format("\nAgent is %s", register ? "registered" : "updated"));
        } else {
            System.out.println(agent + String.format("\nError %s agent", register ? "registering" : "updating"));
        }
    }

    private synchronized void notifyModules() {
        for (AgentListener ai : listeners) {
            if (ai != null) {
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

    public void setHeartbeatTimeoutSec(int heartbeatTimeoutSec) {
        this.heartbeatTimeoutSec = heartbeatTimeoutSec;
    }

    public void setHeartbeatFromMin(int heartbeatFromMin) {
        this.heartbeatFromMin = heartbeatFromMin;
    }

    public void setHeartbeatToMin(int heartbeatToMin) {
        this.heartbeatToMin = heartbeatToMin;
    }

    public void setAgentFreshnessMin(int agentFreshnessMin) {
        this.agentFreshnessMin = agentFreshnessMin;
    }

    @Override
    public Set<Agent> getRegisteredAgents() {
        return persistenceAgent.getRegisteredAgents(agentFreshnessMin);
    }

    @Override
    public Set<Agent> getAgentsToHeartbeat() {
        return persistenceAgent.getAgentsByHeartbeat(heartbeatFromMin, heartbeatToMin);
    }

    public Set<Agent> getRegisteredLxcAgents() {
        return persistenceAgent.getRegisteredLxcAgents(agentFreshnessMin);
    }

    public Set<Agent> getRegisteredPhysicalAgents() {
        return persistenceAgent.getRegisteredPhysicalAgents(agentFreshnessMin);
    }

    /**
     * assume the following: lets say that physical agent's hostname is "py01"
     * then its child lxc agents will be like "py01_lxc_hadoop-node-1"
     *
     * @param physicalAgent - physical agent
     * @return child lxc agents of a physical agent
     */
    public Set<Agent> getChildLxcAgents(Agent physicalAgent) {
        return persistenceAgent.getRegisteredChildLxcAgents(physicalAgent, agentFreshnessMin);
    }
}
