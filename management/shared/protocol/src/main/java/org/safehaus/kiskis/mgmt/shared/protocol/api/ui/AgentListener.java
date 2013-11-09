package org.safehaus.kiskis.mgmt.shared.protocol.api.ui;

import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/8/13
 * Time: 9:34 PM
 */
public interface AgentListener {
    public void agentRegistered(AgentManagerInterface source, AgentInterface module);
}
