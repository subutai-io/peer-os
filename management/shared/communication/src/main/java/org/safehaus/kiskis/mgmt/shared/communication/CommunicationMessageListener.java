package org.safehaus.kiskis.mgmt.shared.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;

import javax.jms.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 12:13 AM
 */
public class CommunicationMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(CommunicationMessageListener.class.getName());
    private final ConcurrentLinkedDeque<BrokerListener> listeners = new ConcurrentLinkedDeque<BrokerListener>();

    public CommunicationMessageListener() {
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

            if (response != null) {
                notifyListeners(response);
            } else {
                System.out.println("Could not parse response");
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error in onMessage", ex);
        }
    }

    private void notifyListeners(Response response) {
        try {
            for (BrokerListener ai : listeners) {
                if (ai != null) {
                    ai.getCommand(response);
                } else {
                    listeners.remove(ai);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notifyListeners", ex);
        }
    }

    public void addListener(BrokerListener listener) {
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    public void removeListener(BrokerListener listener) {
        try {
            listeners.remove(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }

    public void destroy() {
        if (listeners != null) {
            listeners.clear();
        }
    }
}
