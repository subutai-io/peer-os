/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.shared.protocol.api;

import java.util.Set;
import java.util.UUID;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

/**
 *
 * @author dilshat
 */
public interface AgentManager {

    public Set<Agent> getAgents();

    public Set<Agent> getPhysicalAgents();

    public Agent getAgentByHostname(String hostname);

    public Agent getPhysicalAgentByHostnameFromDB(String hostname);

    public Agent getLxcAgentByHostnameFromDB(String hostname);

    public Agent getAgentByUUID(UUID uuid);

    public Set<Agent> getLxcAgents();

    public Set<Agent> getLxcAgentsByParentHostname(String parentHostname);

    public Agent getAgentByUUIDFromDB(UUID uuid);

    public void addListener(AgentListener listener);

    public void removeListener(AgentListener listener);
}
