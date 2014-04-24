/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author dilshat
 */
public class AgentManagerImpl implements ResponseListener, AgentManager {

    private static final Logger LOG = Logger.getLogger(AgentManagerImpl.class.getName());
    /**
     * list of agent listeners
     */
    private final Queue<AgentListener> listeners = new ConcurrentLinkedQueue<AgentListener>();
    /**
     * reference to communication manager
     */
    private CommunicationManager communicationService;
    /**
     * executor for notifying agent listeners
     */
    private ExecutorService exec;
    /**
     * cache of currently connected agents with expiry ttl
     */
    private Cache<UUID, Agent> agents;

    private volatile boolean notifyAgentListeners = true;

    public void setCommunicationService(CommunicationManager communicationService) {
        this.communicationService = communicationService;
    }

    public Collection<AgentListener> getListeners() {
        return Collections.unmodifiableCollection(listeners);
    }

    /**
     * Returns all agents currently connected to the mgmt server.
     *
     * @return set of all agents connected to the mgmt server.
     */
    public Set<Agent> getAgents() {
        return new HashSet(agents.asMap().values());
    }

    /**
     * Returns all physical agents currently connected to the mgmt server.
     *
     * @return set of all physical agents currently connected to the mgmt
     * server.
     */
    public Set<Agent> getPhysicalAgents() {
        Set<Agent> physicalAgents = new HashSet<Agent>();
        for (Agent agent : agents.asMap().values()) {
            if (!agent.isIsLXC()) {
                physicalAgents.add(agent);
            }
        }
        return physicalAgents;
    }

    /**
     * Returns all lxc agents currently connected to the mgmt server.
     *
     * @return set of all lxc agents currently connected to the mgmt server.
     */
    public Set<Agent> getLxcAgents() {
        Set<Agent> lxcAgents = new HashSet<Agent>();
        for (Agent agent : agents.asMap().values()) {
            if (agent.isIsLXC()) {
                lxcAgents.add(agent);
            }
        }
        return lxcAgents;
    }

    /**
     * Returns agent by its node's hostname or null if agent is not connected
     *
     * @param hostname - hostname of agent's node
     * @return agent
     */
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

    /**
     * Returns agent by its UUID or null if agent is not connected
     *
     * @param uuid - UUID of agent
     * @return agent
     */
    public Agent getAgentByUUID(UUID uuid) {
        return agents.getIfPresent(uuid);
    }

    /**
     * Returns agent by its physical parent node's hostname or null if agent is
     * not connected
     *
     * @param parentHostname - hostname of agent's node physical parent node
     * @return agent
     */
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

    /**
     * Adds listener which wants to be notified when agents connect/disconnect
     *
     * @param listener - listener to add
     */
    @Override
    public void addListener(AgentListener listener) {
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    /**
     * Removes listener
     *
     * @param listener - - listener to remove
     */
    @Override
    public void removeListener(AgentListener listener) {
        try {
            listeners.remove(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    /**
     * Initialized agent manager
     */
    public void init() {
        try {

            Preconditions.checkNotNull(communicationService, "Communication service is null");

            agents = CacheBuilder.newBuilder().
                    expireAfterWrite(Common.AGENT_FRESHNESS_MIN, TimeUnit.MINUTES).
                    build();

            communicationService.addListener(this);

            exec = Executors.newSingleThreadExecutor();
            exec.execute(new Runnable() {

                public void run() {
                    while (!Thread.interrupted()) {
                        try {
                            if (notifyAgentListeners) {
                                notifyAgentListeners = false;
                                Set<Agent> freshAgents = new HashSet(agents.asMap().values());
                                for (Iterator<AgentListener> it = listeners.iterator(); it.hasNext(); ) {
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
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                }
            });
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }
    }

    /**
     * Disposes agent manager
     */
    public void destroy() {
        try {
            agents.invalidateAll();
            exec.shutdownNow();
            communicationService.removeListener(this);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    /**
     * Communication manager event when response from agent arrives
     */
    public void onResponse(Response response) {
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                addAgent(response);
                break;
            }
            case HEARTBEAT_RESPONSE: {
                addAgent(response);
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

    /**
     * Adds agent to the cache of connected agents
     */
    private void addAgent(Response response) {
        try {
            if (response != null && response.getUuid() != null) {
                Agent checkAgent = agents.getIfPresent(response.getUuid());
                if (checkAgent != null) {
                    //update timestamp of agent here
                    agents.put(response.getUuid(), checkAgent);
                    return;
                }
                Agent agent = new Agent(response.getUuid(), Strings.isNullOrEmpty(response.getHostname()) ? response.getUuid().toString() : response.getHostname());
                agent.setMacAddress(response.getMacAddress());
                agent.setTransportId(response.getTransportId());
                if (response.isIsLxc() == null) {
                    agent.setIsLXC(false);
                } else {
                    agent.setIsLXC(response.isIsLxc());
                }
                agent.setListIP(response.getIps());
                if (agent.isIsLXC()) {
                    if (agent.getHostname() != null && agent.getHostname().matches(".+" + Common.PARENT_CHILD_LXC_SEPARATOR + ".+")) {
                        agent.setParentHostName(agent.getHostname().substring(0, agent.getHostname().indexOf(Common.PARENT_CHILD_LXC_SEPARATOR)));
                    } else {
                        agent.setParentHostName(Common.UNKNOWN_LXC_PARENT_NAME);
                    }
                }
                sendAck(agent.getUuid());
                agents.put(response.getUuid(), agent);
                notifyAgentListeners = true;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in addAgent", e);
        }
    }

    /**
     * Sends ack to agent when it is registered with the management server
     */
    private void sendAck(UUID agentUUID) {
        Request ack = CommandFactory.newRequest(
                RequestType.REGISTRATION_REQUEST_DONE,
                agentUUID,
                null, null, null, null, null,
                OutputRedirection.NO,
                OutputRedirection.RETURN,
                null, null, null, null, null, null);
        communicationService.sendRequest(ack);
    }

    /**
     * Removes agent from the cache of connected agents
     */
    private void removeAgent(Response response) {
        try {
            if (response != null && response.getTransportId() != null) {
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
