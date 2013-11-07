package org.safehaus.kiskis.mgmt.shared.communication.impl;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

public class CommandTransport implements CommandTransportInterface {

    @Override
    public Response sendCommand(Command command) {
        thread(new CommandProducer(command), false);
        return null;
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class CommandProducer implements Runnable {

        Command command;

        public CommandProducer(Command command) {
            this.command = command;
        }

        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(Common.MQ_HOST);
                Connection connection = connectionFactory.createConnection(Common.MQ_USERNAME, Common.MQ_PASSWORD);
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(command.getCommand().getUuid());
                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String json = CommandJson.getJson(command);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
                session.close();
                connection.close();
            } catch (JMSException e) {
                System.out.println("Caught: " + e);
            }
        }
    }
}
