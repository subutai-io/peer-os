package org.safehaus.kiskis.mgmt.shared.communication.impl;

import java.util.logging.Logger;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.CommandSendInterface;
import org.safehaus.kiskismgmt.protocol.Command;
import org.safehaus.kiskismgmt.protocol.CommandJson;
import org.safehaus.kiskismgmt.protocol.Request;
import org.safehaus.kiskismgmt.protocol.Response;

public class ServerSideAction implements CommandSendInterface {

    private static final Logger LOG = Logger.getLogger(ServerSideAction.class.getName());
    private static final String brokerURL = "tcp://127.0.0.1:61616";
    private static final String brokerUsername = "karaf";
    private static final String brokerPassword = "karaf";

    @Override
    public Response sendRequestToAgent(Request request) {
        thread(new RequestProducer(request), false);
        return null;
    }

    @Override
    public Response sendCommandToAgent(Command command) {
        thread(new CommandProducer(command), false);
        return null;
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class RequestProducer implements Runnable {

        Request request;

        public RequestProducer(Request request) {
            this.request = request;
        }

        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
                Connection connection = connectionFactory.createConnection(brokerUsername, brokerPassword);
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(request.getUuid());
                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String json = CommandJson.getJson(request);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
                session.close();
                connection.close();
            } catch (JMSException e) {
                System.out.println("Caught: " + e);
            }
        }
    }

    public static class CommandProducer implements Runnable {

        Command command;

        public CommandProducer(Command command) {
            this.command = command;
        }

        public void run() {
            try {
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerURL);
                Connection connection = connectionFactory.createConnection(brokerUsername, brokerPassword);
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
