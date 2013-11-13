package org.safehaus.kiskis.mgmt.shared.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;

import javax.jms.*;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 12:13 AM
 */
public class CommunicationMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(CommunicationMessageListener.class.getName());

    private Session session;
    private BrokerInterface brokerService;

    /**
     * @param session
     * @param brokerService
     */
    public CommunicationMessageListener(Session session, BrokerInterface brokerService) {
        this.session = session;
        this.brokerService = brokerService;
    }

    /**
     * Distributes incoming message to appropriate bundles.
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        TextMessage txtMsg = (TextMessage) message;
        try {
            String jsonCmd = txtMsg.getText();
            Response response = CommandJson.getResponse(jsonCmd);
            brokerService.distributeResponse(response);
        } catch (JMSException ex) {
            LOG.info(ex.getMessage());
            System.out.println("onMessage " + ex.getMessage());
        }
    }
}
