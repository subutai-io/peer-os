package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server;


import java.util.Set;
import org.safehaus.kiskismgmt.protocol.Agent;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;


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
