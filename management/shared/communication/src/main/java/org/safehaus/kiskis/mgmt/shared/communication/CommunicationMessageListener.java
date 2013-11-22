package org.safehaus.kiskis.mgmt.shared.communication;

import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;

import javax.jms.*;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA. User: daralbaev Date: 11/8/13 Time: 12:13 AM
 */
public class CommunicationMessageListener implements MessageListener {

    private final Session session;
    private ArrayList<BrokerListener> listeners = new ArrayList<BrokerListener>();

    /**
     * @param session
     */
    public CommunicationMessageListener(Session session) {
        this.session = session;
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
            System.out.println("Received:" + jsonCmd);
            Response response = CommandJson.getResponse(jsonCmd);


            notifyListeners(response);
        } catch (JMSException ex) {
            System.out.println("onMessage " + ex.getMessage());
        }
    }

    private void notifyListeners(Response response) {
        try {
            for (BrokerListener ai : (ArrayList<BrokerListener>) listeners.clone()) {
                if (ai != null) {
                    ai.getCommand(response);
                } else {
                    listeners.remove(ai);
                }
            }
        } catch (Exception ex) {
            System.out.println("Notify listener");
            ex.printStackTrace();
        }
    }

    public synchronized void addListener(BrokerListener listener) {
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void removeListener(BrokerListener listener) {
        try {
            listeners.remove(listener);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void destroy() {
        if (listeners != null) {
            listeners.clear();
        }
    }
}
