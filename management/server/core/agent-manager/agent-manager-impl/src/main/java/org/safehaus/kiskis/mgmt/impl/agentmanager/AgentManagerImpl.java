/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.agentmanager;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.CommunicationManager;
import org.safehaus.kiskis.mgmt.api.communicationmanager.ResponseListener;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
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
     * reference to communication manager
     */
    private CommunicationManager communicationService;
    /**
     * reference to db manager
     */
    private DbManager dbManagerService;
    /**
     * list of agent listeners
     */
    private final Queue<AgentListener> listeners = new ConcurrentLinkedQueue<AgentListener>();
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

    public void setDbManagerService(DbManager dbManagerService) {
        this.dbManagerService = dbManagerService;
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
     * @param hostname - UUID of agent
     * @return agent
     */
    public Agent getAgentByUUID(UUID uuid) {
        return agents.getIfPresent(uuid);
    }

    /**
     * Returns agent by its physical parent node's hostname or null if agent is
     * not connected
     *
     * @param hostname - hostname of agent's node physical parent node
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
     * @param listener
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
     * @param listener
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

    /**
     * Adds agent to the cache of connected agents
     */
    private void addAgent(Response response, boolean register) {
        try {
            if (response != null) {
                if (response.getUuid() == null) {
                    throw new Exception("Error " + (register ? "registering" : "updating") + " agent: UUID is null " + response);
                }
                Agent checkAgent = agents.getIfPresent(response.getUuid());
                if (checkAgent != null) {
                    //update timestamp of agent here
                    agents.put(response.getUuid(), checkAgent);
                    return;
                }
                Agent agent = new Agent(response.getUuid(), response.getHostname());
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
                sendAck(agent.getUuid());
                agents.put(response.getUuid(), agent);
                saveAgent(agent);
                notifyAgentListeners = true;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in addAgent", e);
        }
    }

    /**
     * Sends ack to agent when it is registered with the mgmt server
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
                        deleteAgent(agent);
                        notifyAgentListeners = true;
                        return;
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in removeAgent", e);
        }
    }

    /**
     * Returns lxc agent by its node's hostname from DB or null if agent is not
     * connected
     *
     * @param hostname - hostname of agent's node
     * @return agent
     */
    @Deprecated
    public Agent getLxcAgentByHostnameFromDB(String hostname) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = true and hostname = ? and lastheartbeat >= ? LIMIT 1 ALLOW FILTERING";
            ResultSet rs = dbManagerService.executeQuery(cql, hostname,
                    new Date(System.currentTimeMillis() - Common.AGENT_FRESHNESS_MIN * 60 * 1000));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent(row.getUUID("uuid"), row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getLxcAgentByHostnameFromDB", ex);
        }
        return agent;
    }

    /**
     * Returns physical agent by its node's hostname from DB or null if agent is
     * not connected
     *
     * @param hostname - hostname of agent's node
     * @return agent
     */
    @Deprecated
    public Agent getPhysicalAgentByHostnameFromDB(String hostname) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = false and hostname = ? and lastheartbeat >= ? LIMIT 1 ALLOW FILTERING";
            ResultSet rs = dbManagerService.executeQuery(cql, hostname,
                    new Date(System.currentTimeMillis() - Common.AGENT_FRESHNESS_MIN * 60 * 1000));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent(row.getUUID("uuid"), row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getPhysicalAgentByHostnameFromDB", ex);
        }
        return agent;
    }

    /**
     * Returns agent by its UUIDe from DB or null if agent is not connected
     *
     * @param hostname - UUID of agent's node
     * @return agent
     */
    @Deprecated
    public Agent getAgentByUUIDFromDB(UUID uuid) {
        Agent agent = null;
        try {
            String cql = "select * from agents where uuid = ?";
            ResultSet rs = dbManagerService.executeQuery(cql, uuid);
            Row row = rs.one();
            if (row != null) {
                agent = new Agent(row.getUUID("uuid"), row.getString("hostname"));
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
                agent.setIsLXC(row.getBool("islxc"));
                agent.setLastHeartbeat(row.getDate("lastheartbeat"));
                agent.setListIP(row.getList("listip", String.class));
                agent.setMacAddress(row.getString("macaddress"));
                agent.setParentHostName(row.getString("parenthostname"));
            }

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in getAgentByUUIDFromDB", ex);
        }
        return agent;
    }

    /**
     * Persist agent to DB
     */
    private void saveAgent(Agent agent) {
        try {
            String cql = "select uuid from agents where hostname = ?";
            Set<UUID> uuids = new HashSet<UUID>();
            ResultSet rs = dbManagerService.executeQuery(cql, agent.getHostname());
            if (rs != null) {
                for (Row row : rs) {
                    uuids.add(row.getUUID("uuid"));
                }

            }
            cql = "delete from agents where uuid = ?";

            for (UUID uuid : uuids) {
                dbManagerService.executeUpdate(cql, uuid);
            }

            cql = "insert into agents (uuid, hostname, islxc, listip, macaddress, lastheartbeat,parenthostname,transportid) "
                    + "values (?,?,?,?,?,?,?,?)";

            dbManagerService.executeUpdate(cql, agent.getUuid(),
                    agent.getHostname(), agent.isIsLXC(), agent.getListIP(),
                    agent.getMacAddress(), new Date(),
                    agent.getParentHostName(), agent.getTransportId());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in saveAgent", e);
        }
    }

    /**
     * Removes agent from DB
     */
    private void deleteAgent(Agent agent) {

        String cql = "select uuid from agents where transportid = ?";
        ResultSet rs = dbManagerService.executeQuery(cql, agent.getTransportId());
        Set<UUID> uuids = new HashSet<UUID>();
        if (rs != null) {
            for (Row row : rs) {
                uuids.add(row.getUUID("uuid"));
            }
        }

        cql = "delete from agents where uuid = ?";
        for (UUID uuid : uuids) {
            dbManagerService.executeUpdate(cql, uuid);
        }
    }

}
