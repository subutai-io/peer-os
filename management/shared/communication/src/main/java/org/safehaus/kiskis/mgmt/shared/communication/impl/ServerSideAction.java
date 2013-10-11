package org.safehaus.kiskis.mgmt.shared.communication.impl;

import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.CommandSendInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

public class ServerSideAction implements CommandSendInterface {

    @Override
    public Response sendCommandToAgent(Request request) {
        System.out.println("Command for Agent is send to ActiveMQ");
        return null;  
    }
}
