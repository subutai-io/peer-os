package org.safehaus.kiskis.mgmt.shared.protocol.api.ui;

import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 9:37 PM
 */
public interface AgentListener {

    public void onAgent(Set<Agent> freshAgents);
}
