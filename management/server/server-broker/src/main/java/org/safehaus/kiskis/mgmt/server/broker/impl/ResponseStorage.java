package org.safehaus.kiskis.mgmt.server.broker.impl;

import org.safehaus.kiskis.mgmt.shared.protocol.elements.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: daralbaev
 * Date: 10/10/13
 * Time: 4:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class ResponseStorage implements RegisteredHostInterface {
    @Override
    public List<Agent> getRegisteredHosts() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Response sendAgentResponse(Response response) {
        //TO-DO Create result from Response for ui
        System.out.println("Received response from communication: " + response.toString());
        System.out.println();
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
