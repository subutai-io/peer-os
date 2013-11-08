package org.safehaus.kiskis.mgmt.server.broker.impl;

import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 10/10/13 Time: 4:48 PM To
 * change this template use File | Settings | File Templates.
 */
public class Broker implements BrokerInterface {

    private CommandManagerInterface commandManager;
    private AgentManagerInterface agentManager;

    /**
     * For Communication Bundle
     *
     * @param response
     * @return
     */
    @Override
    public Request distributeResponse(Response response) {
        Request req = null;
        System.out.println(this.getClass().getName() + " distribute is called");
        //TO-DO Distribute response to Agent or Command Bundle
        switch (response.getType()) {
            case REGISTRATION_REQUEST: {
                Agent agent = new Agent();
                agent.setUuid(response.getUuid());
                agent.setHostname(response.getHostname());
                agent.setMacAddress(response.getMacAddress());
                agentManager.registerAgent(agent);
                break;
            }

        }
        return req;
    }

    public void setCommandManagerService(CommandManagerInterface commandManager) {
        this.commandManager = commandManager;
        System.out.println(this.getClass().getName() + " CommandManagerInterface initialized");
    }

    public void setAgentManagerService(AgentManagerInterface agentManager) {
        this.agentManager = agentManager;
        System.out.println(this.getClass().getName() + " AgentManagerInterface initialized");
    }
}
