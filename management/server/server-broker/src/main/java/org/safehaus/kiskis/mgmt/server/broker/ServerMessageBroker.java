package org.safehaus.kiskis.mgmt.server.broker;

import org.safehaus.kiskis.mgmt.shared.communication.interfaces.server.IServerManager;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.IGenerator;


import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;
import java.util.logging.Logger;

public class ServerMessageBroker implements IServerManager, MessageListener {

    private static final Logger LOG = Logger.getLogger(ServerMessageBroker.class.getName());
    private IGenerator generator;
    Session session;

    public ServerMessageBroker(Session session, IGenerator generator) {
        this.session = session;
        this.generator = generator;
    }


    @Override
    public List<Agent> getRegisteredHosts() {
        return null;
    }

    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        try {
            String jsonCmd = txtMsg.getText();

            if(generator != null){
                Command comm = generator.fromJson(jsonCmd);
                System.out.println(comm.toString());
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
