package org.safehaus.kiskis.mgmt.shared.communication;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;

import javax.jms.*;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.OldestMessageWithLowestPriorityEvictionStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;

public class CommandTransport implements CommandTransportInterface {

    private static final Logger LOG = Logger.getLogger(CommandTransport.class.getName());
    private BrokerService broker;
    private int amqPort;
    private String amqHost;
    private String amqBindAddress;
    private String amqServiceQueue;
    private String amqBrokerCertificateName;
    private String amqBrokerTrustStoreName;
    private String amqBrokerCertificatePwd;
    private String amqBrokerTrustStorePwd;
    private Session listenerSession;
    private CommunicationMessageListener communicationMessageListener;

    public void setAmqPort(int amqPort) {
        this.amqPort = amqPort;
    }

    public void setAmqHost(String amqHost) {
        this.amqHost = amqHost;
    }

    public void setAmqBindAddress(String amqBindAddress) {
        this.amqBindAddress = amqBindAddress;
    }

    public void setAmqServiceQueue(String amqServiceQueue) {
        this.amqServiceQueue = amqServiceQueue;
    }

    public void setAmqBrokerCertificateName(String amqBrokerCertificateName) {
        this.amqBrokerCertificateName = amqBrokerCertificateName;
    }

    public void setAmqBrokerTrustStoreName(String amqBrokerTrustStoreName) {
        this.amqBrokerTrustStoreName = amqBrokerTrustStoreName;
    }

    public void setAmqBrokerCertificatePwd(String amqBrokerCertificatePwd) {
        this.amqBrokerCertificatePwd = amqBrokerCertificatePwd;
    }

    public void setAmqBrokerTrustStorePwd(String amqBrokerTrustStorePwd) {
        this.amqBrokerTrustStorePwd = amqBrokerTrustStorePwd;
    }

    @Override
    public Response sendCommand(Command command) {
        thread(new CommandProducer(command, amqHost, amqPort), false);
        return null;
    }

    public static void thread(Runnable runnable, boolean daemon) {
        Thread brokerThread = new Thread(runnable);
        brokerThread.setDaemon(daemon);
        brokerThread.start();
    }

    public static class CommandProducer implements Runnable {

        String host;
        int port;
        Command command;

        public CommandProducer(Command command, String host, int port) {
            this.command = command;
            this.port = port;
            this.host = host;
        }

        public void run() {
            try {
//                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("ssl://" + host + ":" + port);
                ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
                Connection connection = connectionFactory.createConnection();
                connection.start();
                Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(command.getCommand().getUuid());
                javax.jms.MessageProducer producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String json = CommandJson.getJson(command);
                System.out.println("Sending: " + json);
                TextMessage message = session.createTextMessage(json);
                producer.send(message);
                session.close();
                connection.close();
            } catch (JMSException ex) {
                LOG.log(Level.SEVERE, "Error in CommandProducer.run", ex);
            }
        }
    }

    public void init() {

        try {
            listenerSession.close();
        } catch (Exception e) {
        }
        try {

            broker.stop();
            broker.waitUntilStopped();
        } catch (Exception e) {
        }

        try {
            System.setProperty("javax.net.ssl.keyStore", System.getProperty("karaf.base") + "/" + this.amqBrokerCertificateName);
            System.setProperty("javax.net.ssl.keyStorePassword", this.amqBrokerCertificatePwd);
            System.setProperty("javax.net.ssl.trustStore", System.getProperty("karaf.base") + "/" + this.amqBrokerTrustStoreName);
            System.setProperty("javax.net.ssl.trustStorePassword", this.amqBrokerTrustStorePwd);

            broker = new BrokerService();
            //***policy
            PolicyMap policy = new PolicyMap();
            PolicyEntry pentry = new PolicyEntry();
            OldestMessageWithLowestPriorityEvictionStrategy eviction = new OldestMessageWithLowestPriorityEvictionStrategy();
            eviction.setEvictExpiredMessagesHighWatermark(1000);
            pentry.setMessageEvictionStrategy(eviction);
            ConstantPendingMessageLimitStrategy limit = new ConstantPendingMessageLimitStrategy();
            limit.setLimit(100);
            pentry.setPendingMessageLimitStrategy(limit);
            policy.setDefaultEntry(pentry);
            broker.setDestinationPolicy(policy);
            //***policy
            broker.setPersistent(true);
            broker.setUseJmx(false);
            broker.addConnector("ssl://" + this.amqBindAddress + ":" + this.amqPort);
            broker.addConnector("amqp://0.0.0.0:5672?maximumConnections=1000&wireformat.maxFrameSize=104857600");
            broker.start();
            broker.waitUntilStarted();
            setupListener();
            System.out.println("ActiveMQ started...");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }

    }

    public void destroy() {
        try {
            broker.stop();
            communicationMessageListener.destroy();
            System.out.println("ActiveMQ stopped...");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    private void setupListener() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
//        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("ssl://" + this.amqHost + ":" + this.amqPort);
        Connection connection;
        try {
            connection = connectionFactory.createConnection();
            connection.start();
            listenerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination adminQueue = listenerSession.createQueue(this.amqServiceQueue);

            MessageConsumer consumer = listenerSession.createConsumer(adminQueue);
            communicationMessageListener = new CommunicationMessageListener();
            consumer.setMessageListener(communicationMessageListener);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error in setupListener", ex);
        }
    }

    @Override
    public synchronized void addListener(BrokerListener listener) {
        communicationMessageListener.addListener(listener);
    }

    @Override
    public synchronized void removeListener(BrokerListener listener) {
        communicationMessageListener.removeListener(listener);
    }
}
