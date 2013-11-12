package org.safehaus.kiskis.mgmt.shared.communication;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

import javax.jms.*;

public class CommandTransport implements CommandTransportInterface {

    private BrokerService broker;
    private BrokerInterface brokerService;

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
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("ssl://" + Common.MQ_HOST + ":" + Common.MQ_PORT);
                Connection connection = connectionFactory.createConnection();
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

    public void setBrokerService(BrokerInterface brokerService) {
        this.brokerService = brokerService;
        if (brokerService != null) {
            System.out.println("......." + this.getClass().getName() + " BrokerInterface initialized");
        } else {
            System.out.println("......." + this.getClass().getName() + " BrokerInterface not initialized");
        }

    }

    public void init() {
        try {
            System.setProperty("javax.net.ssl.keyStore", System.getProperty("karaf.base") + "/broker.ks");
            System.setProperty("javax.net.ssl.keyStorePassword", "broker");
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("karaf.base") + "/client.ts");
            System.setProperty("javax.net.ssl.trustStorePassword", "client");


            broker = new BrokerService();
            broker.setPersistent(true);
            broker.setUseJmx(false);
            broker.addConnector("ssl://0.0.0.0:" + Common.MQ_PORT);
            broker.start();
            setupListener();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    public void destroy() {
        try {
            broker.stop();
        } catch (Exception ex) {
            System.out.println(ex.toString());
        }
    }

    private void setupListener() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("ssl://" + Common.MQ_HOST + ":" + Common.MQ_PORT);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination adminQueue = session.createQueue(Common.MQ_SERVICE_QUEUE);

            MessageConsumer consumer = session.createConsumer(adminQueue);
            consumer.setMessageListener(new CommunicationMessageListener(session, brokerService));
            System.out.println("ActiveMQ started...");
        } catch (JMSException ex) {
            System.out.println(ex.toString());
        }
    }
}
