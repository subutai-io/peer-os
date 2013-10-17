package org.safehaus.kiskis.mgmt.shared.communication.impl;

import org.safehaus.kiskis.mgmt.shared.communication.Activator;
import org.safehaus.kiskis.mgmt.shared.communication.util.JsonGenerator;
import org.safehaus.kiskismgmt.protocol.Response;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.safehaus.kiskismgmt.protocol.CommandJson;
import org.safehaus.kiskismgmt.protocol.ICommand;
import org.safehaus.kiskismgmt.protocol.Request;

public class ServerMessageBroker implements MessageListener {

    Session session;

    public ServerMessageBroker(Session session) {
        this.session = session;
    }

    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        try {
            String jsonCmd = txtMsg.getText();

            System.out.println("Shared Communication got TextMessage :" + jsonCmd);
            Response response = CommandJson.getResponse(jsonCmd);
            System.out.println("RESPONSE " + response.toString());
            Request req = Activator.getServerBroker().sendAgentResponse(response);
        } catch (Exception ex) {
            System.out.println("EXCEPTION IN on MESSGE " + ex);
        }
    }
}
