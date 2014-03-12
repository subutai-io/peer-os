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
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentManager;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.api.communication.Communication;
import org.safehaus.kiskis.mgmt.api.dbmanager.DbManager;
import org.safehaus.kiskis.mgmt.api.communication.ResponseListener;
import org.safehaus.kiskis.mgmt.api.agentmanager.AgentListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

/**
 *
 * @author dilshat
 */
public class AgentManagerImpl implements ResponseListener, AgentManager {

    private static final Logger LOG = Logger.getLogger(AgentManagerImpl.class.getName());

    private Communication communicationService;
    private DbManager dbManagerService;
    private final Queue<AgentListener> listeners = new ConcurrentLinkedQueue<AgentListener>();
    private ExecutorService exec;
    private Cache<UUID, Agent> agents;
    private volatile boolean notifyAgentListeners = true;

    public void setCommunicationService(Communication communicationService) {
        this.communicationService = communicationService;
    }

    public void setDbManagerService(DbManager dbManagerService) {
        this.dbManagerService = dbManagerService;
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

    public Agent getLxcAgentByHostnameFromDB(String hostname) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = true and hostname = ? and lastheartbeat >= ? LIMIT 1 ALLOW FILTERING";
            ResultSet rs = dbManagerService.executeQuery(cql, hostname,
                    new Date(System.currentTimeMillis() - Common.AGENT_FRESHNESS_MIN * 60 * 1000));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
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

    public Agent getPhysicalAgentByHostnameFromDB(String hostname) {
        Agent agent = null;
        try {
            String cql = "select * from agents where islxc = false and hostname = ? and lastheartbeat >= ? LIMIT 1 ALLOW FILTERING";
            ResultSet rs = dbManagerService.executeQuery(cql, hostname,
                    new Date(System.currentTimeMillis() - Common.AGENT_FRESHNESS_MIN * 60 * 1000));
            Row row = rs.one();
            if (row != null) {
                agent = new Agent();
                agent.setUuid(row.getUUID("uuid"));
                agent.setHostname(row.getString("hostname"));
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

    public Agent getAgentByUUIDFromDB(UUID uuid) {
        Agent agent = new Agent();
        try {
            String cql = "select * from agents where uuid = ?";
            ResultSet rs = dbManagerService.executeQuery(cql, uuid);
            for (Row row : rs) {
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
            communicationService.removeListener(this);
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
                sendAck(agent.getUuid());
                agents.put(response.getUuid(), agent);
                saveAgent(agent);
                notifyAgentListeners = true;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in addAgent", e);
        }
    }

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

    private void saveAgent(Agent agent) {
        try {
            String cql = "select uuid from agents where hostname = ?";
            ResultSet rs = dbManagerService.executeQuery(cql, agent.getHostname());
            Set<UUID> uuids = new HashSet<UUID>();
            for (Row row : rs) {
                uuids.add(row.getUUID("uuid"));
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

    private void deleteAgent(Agent agent) {

        String cql = "select uuid from agents where transportid = ?";
        ResultSet rs = dbManagerService.executeQuery(cql, agent.getTransportId());
        Set<UUID> uuids = new HashSet<UUID>();
        for (Row row : rs) {
            uuids.add(row.getUUID("uuid"));
        }

        cql = "delete from agents where uuid = ?";
        for (UUID uuid : uuids) {
            dbManagerService.executeUpdate(cql, uuid);
        }
    }

}
