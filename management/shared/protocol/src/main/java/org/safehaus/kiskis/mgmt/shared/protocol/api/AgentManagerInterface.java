package org.safehaus.kiskis.mgmt.shared.protocol.api;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ui.AgentListener;

import java.util.Set;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/7/13 Time: 10:40 PM
 */
public interface AgentManagerInterface {

    public Set<Agent> getRegisteredAgents();

    public void addListener(AgentListener listener);

    public void removeListener(AgentListener listener);
}
