/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.agent;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AgentManagerImpl implements ResponseListener, org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManager {

    private static final Logger LOG = Logger.getLogger(AgentManagerImpl.class.getName());

    private CommandTransportInterface commandTransportInterface;
    private final Queue<AgentListener> listeners = new ConcurrentLinkedQueue<AgentListener>();
    private ExecutorService exec;
    private Cache<UUID, Agent> agents;
    private volatile boolean notifyAgentListeners = true;
    private int agentFreshnessMin;

    public void setAgentFreshnessMin(int agentFreshnessMin) {
        this.agentFreshnessMin = agentFreshnessMin;
    }

    public void setCommandTransportInterface(CommandTransportInterface commandTransportInterface) {
        this.commandTransportInterface = commandTransportInterface;
    }

    public Set<Agent> getAgents() {
        return new HashSet(agents.asMap().values());
    }

    public Set<Agent> getPhysicalAgents() {
        Set<Agent> physicalAgents = new HashSet<Agent>();
        for (Agent agent : agents.asMap().values()) {
            if (!agent.isIsLXC()) {
                physicalAgents.add(agent);
            }
        }
        return physicalAgents;
    }

    public Agent getAgentByHostname(String hostname) {
        if (!Util.isStringEmpty(hostname)) {
            for (Agent agent : agents.asMap().values()) {
                if (hostname.equalsIgnoreCase(agent.getHostname())) {
                    return agent;
                }
            }
        }
        return null;
    }

    public Agent getAgentByUUID(UUID uuid) {
        return agents.getIfPresent(uuid);
    }

    public Set<Agent> getLxcAgents() {
        Set<Agent> lxcAgents = new HashSet<Agent>();
        for (Agent agent : agents.asMap().values()) {
            if (agent.isIsLXC()) {
                lxcAgents.add(agent);
            }
        }
        return lxcAgents;
    }

    public Set<Agent> getLxcAgentsByParentHostname(String parentHostname) {
        Set<Agent> lxcAgents = new HashSet<Agent>();
        if (!Util.isStringEmpty(parentHostname)) {
            for (Agent agent : agents.asMap().values()) {
                if (parentHostname.equalsIgnoreCase(agent.getParentHostName())) {
                    lxcAgents.add(agent);
                }
            }
        }
        return lxcAgents;
    }

    @Override
    public void addListener(AgentListener listener) {
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public void removeListener(AgentListener listener) {
        try {
            listeners.remove(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    public void init() {
        try {

            commandTransportInterface.addListener(this);
            agents = CacheBuilder.newBuilder().
                    expireAfterWrite(agentFreshnessMin, TimeUnit.MINUTES).
                    build();
            exec = Executors.newSingleThreadExecutor();
            exec.execute(new Runnable() {

                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            if (notifyAgentListeners) {
                                notifyAgentListeners = false;
                                List<Agent> freshAgents = new ArrayList(agents.asMap().values());
                                for (Iterator<AgentListener> it = listeners.iterator(); it.hasNext();) {
                                    AgentListener listener = it.next();
                                    try {
                                        listener.onAgent(freshAgents);
                                    } catch (Exception e) {
                                        it.remove();
                                        LOG.log(Level.SEVERE, "Error notifying agent listeners, removing faulting listener", e);
                                    }
                                }
                            }
                            Thread.sleep(1000);
                        } catch (Exception ex) {
                            LOG.log(Level.SEVERE, "Error in AgentNotifier.run", ex);
                        }
                    }
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    public void destroy() {
        try {
            agents.invalidateAll();
            exec.shutdownNow();
            commandTransportInterface.removeListener(this);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    public void onResponse(Response response) {
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                addAgent(response, true);
                break;
            }
            case HEARTBEAT_RESPONSE: {
                addAgent(response, false);
                break;
            }
            case AGENT_DISCONNECT: {
                removeAgent(response);
                break;
            }
            default: {
                break;
            }
        }
    }

    private void addAgent(Response response, boolean register) {
        try {
            if (response.getUuid() == null) {
                throw new Exception("Error " + (register ? "registering" : "updating") + " agent: UUID is null " + response);
            }
            Agent checkAgent = agents.getIfPresent(response.getUuid());
            if (checkAgent != null) {
                //update timestamp of agent here
                agents.put(response.getUuid(), checkAgent);
                return;
            }
            Agent agent = new Agent();
            agent.setUuid(response.getUuid());
            agent.setHostname(response.getHostname());
            agent.setMacAddress(response.getMacAddress());
            agent.setTransportId(response.getTransportId());
            if (response.isIsLxc() == null) {
                agent.setIsLXC(false);
            } else {
                agent.setIsLXC(response.isIsLxc());
            }
            agent.setListIP(response.getIps());
            if (agent.getHostname() == null || agent.getHostname().trim().isEmpty()) {
                agent.setHostname(agent.getUuid().toString());
            }
            if (agent.isIsLXC()) {
                if (agent.getHostname() != null && agent.getHostname().matches(".+" + Common.PARENT_CHILD_LXC_SEPARATOR + ".+")) {
                    agent.setParentHostName(agent.getHostname().substring(0, agent.getHostname().indexOf(Common.PARENT_CHILD_LXC_SEPARATOR)));
                } else {
                    agent.setParentHostName(Common.UNKNOWN_LXC_PARENT_NAME);
                }
            }

            agents.put(response.getUuid(), agent);
            notifyAgentListeners = true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in addAgent", e);
        }
    }

    private void removeAgent(Response response) {
        try {
            if (response.getTransportId() != null) {
                for (Agent agent : agents.asMap().values()) {
                    if (response.getTransportId().equalsIgnoreCase(agent.getTransportId())) {
                        agents.invalidate(agent.getUuid());
                        notifyAgentListeners = true;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in removeAgent", e);
        }
    }

}
