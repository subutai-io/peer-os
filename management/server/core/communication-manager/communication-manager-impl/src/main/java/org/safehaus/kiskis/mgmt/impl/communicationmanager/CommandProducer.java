/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.communicationmanager;

import org.safehaus.kiskis.mgmt.api.communicationmanager.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is used internally by CommunicationManagerImpl for sending
 * requests to agents.
 *
 * @author dilshat
 */
class CommandProducer implements Runnable {

    private final Request command;
    private final CommunicationManagerImpl communicationManagerImpl;
    private static final Logger LOG = Logger.getLogger(CommandProducer.class.getName());

    public CommandProducer(Request command, CommunicationManagerImpl communicationManagerImpl) {
        this.communicationManagerImpl = communicationManagerImpl;
        this.command = command;
    }

    /**
     * Called by executor to send message to agent
     */
    public void run() {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = communicationManagerImpl.getPooledConnectionFactory().createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createQueue(command.getUuid().toString());
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            producer.setTimeToLive(communicationManagerImpl.getAmqMaxMessageToAgentTtlSec() * 1000);
            String json = CommandJson.getJson(command);
            if (command.getType() != RequestType.HEARTBEAT_REQUEST) {
                LOG.log(Level.INFO, "\nSending: {0}", json);
            }
            TextMessage message = session.createTextMessage(json);
            producer.send(message);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error in CommandProducer.run", ex);
        } finally {
            if (producer != null) {
                try {
                    producer.close();
                } catch (Exception e) {
                }
            }
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                }
            }
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                }
            }
        }
    }

}
