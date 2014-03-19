package org.safehaus.kiskis.mgmt.impl.communication;

import java.util.Iterator;
import org.safehaus.kiskis.mgmt.api.communication.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.api.communication.ResponseListener;

import javax.jms.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.RemoveInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.ResponseType;

public class CommunicationMessageListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(CommunicationMessageListener.class.getName());
    private final ConcurrentLinkedQueue<ResponseListener> listeners = new ConcurrentLinkedQueue<ResponseListener>();

    /**
     * Distributes incoming message to appropriate bundles.
     *
     * @param message
     */
    @Override
    public void onMessage(Message message) {
        try {
            if (message instanceof TextMessage) {
                TextMessage txtMsg = (TextMessage) message;
                String jsonCmd = txtMsg.getText();
                Response response = CommandJson.getResponse(jsonCmd);
                if (response != null) {
                    if (response.getType() != ResponseType.HEARTBEAT_RESPONSE) {
                        LOG.log(Level.INFO, "\nReceived {0}", CommandJson.getJson(CommandJson.getCommand(jsonCmd)));
                    }
                    response.setTransportId(((ActiveMQTextMessage) message).getProducerId().toString());
                    notifyListeners(response);
                } else {
                    LOG.log(Level.WARNING, "Could not parse response{0}", jsonCmd);
                }

            } else if (message instanceof ActiveMQMessage) {
                ActiveMQMessage aMsg = (ActiveMQMessage) message;
                if (aMsg.getDataStructure() instanceof RemoveInfo) {
                    Response agentDisconnect = new Response();
                    agentDisconnect.setType(ResponseType.AGENT_DISCONNECT);
                    agentDisconnect.setTransportId(((RemoveInfo) aMsg.getDataStructure()).getObjectId().toString());
                    notifyListeners(agentDisconnect);
                }
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error in onMessage", ex);
        }
    }

    private void notifyListeners(Response response) {
        try {
            for (Iterator<ResponseListener> it = listeners.iterator(); it.hasNext();) {
                ResponseListener ai = it.next();
                try {
                    ai.onResponse(response);
                } catch (Exception e) {
                    it.remove();
                    LOG.log(Level.SEVERE, "Error notifying message listeners, removing faulting listener", e);
                }
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in notifyListeners", ex);
        }
    }

    public void addListener(ResponseListener listener) {
        try {
            listeners.add(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    public void removeListener(ResponseListener listener) {
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
