package org.safehaus.kiskis.mgmt.shared.protocol.api.ui;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.List;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 11/8/13
 * Time: 9:37 PM
 */
public interface AgentListener {
    public void agentRegistered(Set<Agent> agents);

    public int getId();
}
