package org.safehaus.kiskis.mgmt.server.broker;

import com.google.gson.Gson;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.IGenerator;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.IServerManager;

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
    Gson gson = new Gson();

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

        System.out.println(txtMsg);
        System.out.println();
        try {
            String jsonCmd = txtMsg.getText();

            if(generator != null){
                System.out.println(generator.fromJson(jsonCmd));
            }
            Response res = gson.fromJson(jsonCmd, Response.class);
            System.out.println(res);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
}
