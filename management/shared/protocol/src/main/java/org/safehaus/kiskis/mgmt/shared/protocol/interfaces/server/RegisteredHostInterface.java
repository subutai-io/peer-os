package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

import java.util.List;

/**
 * Used by Management Server components query and manage the managed Agents
 */
public interface RegisteredHostInterface {

    /**
     * Gets the agent hosts that are registered with the management server
     *
     * @return the registered hosts
     */
    List<Agent> getRegisteredHosts();

    Response sendAgentResponse(Response response);
}
