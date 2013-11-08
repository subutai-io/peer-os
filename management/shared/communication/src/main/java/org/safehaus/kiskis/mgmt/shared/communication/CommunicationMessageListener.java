package org.safehaus.kiskis.mgmt.shared.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;

import javax.jms.*;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 12:13 AM
 */
public class CommunicationMessageListener implements MessageListener {

    private Session session;
    private BrokerInterface brokerService;

    public CommunicationMessageListener(Session session, BrokerInterface brokerService) {
        this.session = session;
        this.brokerService = brokerService;
    }

    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        try {
            String jsonCmd = txtMsg.getText();
            Response response = CommandJson.getResponse(jsonCmd);
            Request req = brokerService.distribute(response);
        } catch (JMSException ex) {
            System.out.println("onMessage " + ex.getMessage());
        }
    }
}
