package org.safehaus.kiskis.mgmt.server.broker.impl;

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
    public Request distribute(Response response) {
        Request req = null;
        //TO-DO Distribute response to Agent or Command Bundle
        System.out.println(response.toString());
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
