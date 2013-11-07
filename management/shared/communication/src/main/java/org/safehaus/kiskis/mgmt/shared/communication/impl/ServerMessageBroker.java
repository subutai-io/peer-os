package org.safehaus.kiskis.mgmt.shared.communication.impl;

import javax.jms.JMSException;
import org.safehaus.kiskis.mgmt.shared.communication.Activator;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;

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
            Response response = CommandJson.getResponse(jsonCmd);
            Request req = Activator.getServerBroker().distribute(response);
        } catch (JMSException ex) {
            System.out.println("onMessage " + ex.getMessage());
        }
    }
}
