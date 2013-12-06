package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:40 PM
 */
public interface AgentManagerInterface {

    public Agent getAgent(UUID uuid);

    public List<Agent> getRegisteredAgents();

    public List<Agent> getRegisteredLxcAgents();

    public List<Agent> getRegisteredPhysicalAgents();

    public List<Agent> getChildLxcAgents(Agent agent);

    public List<Agent> getUnknownChildLxcAgents();

    public List<Agent> getAgentsToHeartbeat();

    public void addListener(AgentListener listener);

    public void removeListener(AgentListener listener);
}
