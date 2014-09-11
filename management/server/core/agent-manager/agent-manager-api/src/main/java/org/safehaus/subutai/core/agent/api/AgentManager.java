/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.subutai.core.agent.api;


import org.safehaus.subutai.common.protocol.Agent;

import java.util.Set;
import java.util.UUID;


/**
 * Agent Manager provides methods for working with connected agents
 */
public interface AgentManager {

    /**
     * Returns all agents currently connected to the mgmt server.
     *
     * @return set of all agents connected to the mgmt server.
     */
    public Set<Agent> getAgents();

    /**
     * Returns all physical agents currently connected to the mgmt server.
     *
     * @return set of all physical agents currently connected to the mgmt server.
     */
    public Set<Agent> getPhysicalAgents();

    /**
     * Returns all lxc agents currently connected to the mgmt server.
     *
     * @return set of all lxc agents currently connected to the mgmt server.
     */
    public Set<Agent> getLxcAgents();

    /**
     * Returns agent by its node's hostname or null if agent is not connected
     *
     * @param hostname - hostname of agent's node
     * @return agent
     */
    public Agent getAgentByHostname(String hostname);

    /**
     * Returns agent by its UUID or null if agent is not connected
     *
     * @param uuid - UUID of agent
     * @return agent
     */
    public Agent getAgentByUUID(UUID uuid);

    /**
     * Returns agent by its physical parent node's hostname or null if agent is not connected
     *
     * @param parentHostname - hostname of agent's node physical parent node
     * @return agent
     */
    public Set<Agent> getLxcAgentsByParentHostname(String parentHostname);

    /**
     * Adds listener which wants to be notified when agents connect/disconnect
     *
     * @param listener - listener to add
     */
    public void addListener(AgentListener listener);

    /**
     * Removes listener
     *
     * @param listener - - listener to remove
     */
    public void removeListener(AgentListener listener);

    public Set<Agent> getAgentsByHostnames(Set<String> hostnames);
}
