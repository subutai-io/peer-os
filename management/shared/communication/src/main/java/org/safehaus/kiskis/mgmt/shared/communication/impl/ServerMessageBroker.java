package org.safehaus.kiskis.mgmt.shared.communication.impl;

import org.safehaus.kiskis.mgmt.shared.communication.Activator;
import org.safehaus.kiskis.mgmt.shared.communication.util.JsonGenerator;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Request;

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

            System.out.println("Message :" + jsonCmd);
            System.out.println();
            
            Response comm = JsonGenerator.fromJson(jsonCmd);
            System.out.println("Communication service got:" + comm.toString());
            System.out.println();

            Request req = Activator.getServerBroker().sendAgentResponse(comm);
            System.out.println("Request:" + req);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
