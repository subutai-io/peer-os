package org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server;


import java.util.List;
import java.util.Set;

import org.safehaus.kiskis.mgmt.shared.protocol.commands.CommandEnum;
import org.safehaus.kiskismgmt.protocol.*;


/**
 * Used by Management Server components query and manage the managed Agents
 */
public interface RegisteredHostInterface {

    /**
     * For UI Bundle
     * Gets the agent hosts that are registered with the management server
     *
     * @return the registered hosts
     */
    public Set<Agent> getRegisteredHosts();

    /**
     * For UI Bundle
     * @return
     */
    public Set<Product> getRegisteredProducts();

    /**
     * For UI Bundle
     * @param agent
     * @return
     */
    public List<AgentOutput> getAgentOutput(Agent agent);

    /**
     * For UI Bundle
     * @param agent
     * @return
     */
    public Boolean execCommand(Agent agent, String command);

    /**
     * For Communication Bundle
     * @param response
     * @return
     */
    public Request sendAgentResponse(Response response);

}
