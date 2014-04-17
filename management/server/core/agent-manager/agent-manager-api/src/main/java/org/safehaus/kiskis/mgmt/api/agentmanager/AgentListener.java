package org.safehaus.kiskis.mgmt.api.agentmanager;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Set;

/**
 * This interface is used by clients which want to be notified when connected
 * set of connected agents is modified. The onAgent event happens when an agent
 * connects or disconnects from the mgmt server. The freshAgents argument
 * supplies the set of agents currently connected to the mgmt server.
 *
 * @author dilshat
 */
public interface AgentListener {

    public void onAgent(Set<Agent> freshAgents);
}
