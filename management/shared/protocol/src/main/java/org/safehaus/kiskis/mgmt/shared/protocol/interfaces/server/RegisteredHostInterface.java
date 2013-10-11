package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server;


import java.util.Set;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;


/**
 * Used by Management Server components query and manage the managed Agents
 */
public interface RegisteredHostInterface {

    /**
     * Gets the agent hosts that are registered with the management server
     *
     * @return the registered hosts
     */
    public Set<Agent> getRegisteredHosts();

    public Request sendAgentResponse(Response response);

}
