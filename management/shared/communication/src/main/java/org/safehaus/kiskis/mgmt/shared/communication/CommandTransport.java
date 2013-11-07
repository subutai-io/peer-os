package org.safehaus.kiskis.mgmt.shared.communication;

import javax.jms.*;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.settings.Common;

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

    public void setBrokerService(BrokerInterface brokerService) {
        this.brokerService = brokerService;
        System.out.println(this.getClass().getName() + " BrokerInterface initialized");
    }

    public void init() {
        try {
            broker = new BrokerService();
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("tcp://0.0.0.0:" + Common.MQ_PORT);
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
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://" + Common.MQ_HOST + ":" + Common.MQ_PORT);
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
