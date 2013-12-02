package org.safehaus.kiskis.mgmt.shared.communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.api.BrokerListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandTransportInterface;

import javax.jms.*;
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.OldestMessageWithLowestPriorityEvictionStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
//import org.apache.activemq.pool.PooledConnectionFactory;

public class CommandTransport implements CommandTransportInterface {

    private static final Logger LOG = Logger.getLogger(CommandTransport.class.getName());
    private BrokerService broker;
//    private PooledConnectionFactory pooledConnectionFactory;
    private ActiveMQConnectionFactory connectionFactory;
    private CommunicationMessageListener communicationMessageListener;
    private ExecutorService exec;
    private String amqBindAddress;
    private String amqServiceQueue;
    private String amqBrokerCertificateName;
    private String amqBrokerTrustStoreName;
    private String amqBrokerCertificatePwd;
    private String amqBrokerTrustStorePwd;
    private int amqPort;
    private int amqExpiredMessagesHighWatermark;
    private int amqConstantPendingMessageLimit;
    private int amqExpireMessagesPeriodSec;
    private int amqOfflineDurableSubscriberTimeoutSec;
    private int amqOfflineDurableSubscriberTaskScheduleSec;
//    private int amqMaxConnections;
    private int amqExecutorPoolSize;

    public void setAmqPort(int amqPort) {
        this.amqPort = amqPort;
    }

//    public void setAmqMaxConnections(int amqMaxConnections) {
//        this.amqMaxConnections = amqMaxConnections;
//    }
    public void setAmqExecutorPoolSize(int amqExecutorPoolSize) {
        this.amqExecutorPoolSize = amqExecutorPoolSize;
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

    public void setAmqExpiredMessagesHighWatermark(int amqExpiredMessagesHighWatermark) {
        this.amqExpiredMessagesHighWatermark = amqExpiredMessagesHighWatermark;
    }

    public void setAmqConstantPendingMessageLimit(int amqConstantPendingMessageLimit) {
        this.amqConstantPendingMessageLimit = amqConstantPendingMessageLimit;
    }

    public void setAmqExpireMessagesPeriodSec(int amqExpireMessagesPeriodSec) {
        this.amqExpireMessagesPeriodSec = amqExpireMessagesPeriodSec;
    }

    public void setAmqOfflineDurableSubscriberTimeoutSec(int amqOfflineDurableSubscriberTimeoutSec) {
        this.amqOfflineDurableSubscriberTimeoutSec = amqOfflineDurableSubscriberTimeoutSec;
    }

    public void setAmqOfflineDurableSubscriberTaskScheduleSec(int amqOfflineDurableSubscriberTaskScheduleSec) {
        this.amqOfflineDurableSubscriberTaskScheduleSec = amqOfflineDurableSubscriberTaskScheduleSec;
    }

    @Override
    public void sendCommand(Command command) {
        exec.submit(new CommandProducer(command));
    }

    public class CommandProducer implements Runnable {

        Command command;

        public CommandProducer(Command command) {
            this.command = command;
        }

        public void run() {
            Connection connection = null;
            Session session = null;
            MessageProducer producer = null;
            try {
//                connection = pooledConnectionFactory.createConnection();
                connection = connectionFactory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(command.getCommand().getUuid().toString());
                producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                String json = CommandJson.getJson(command);
                System.out.println("Sending: " + json);
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

    public void init() {

//        if (pooledConnectionFactory != null) {
//            try {
//                pooledConnectionFactory.stop();
//            } catch (Exception e) {
//            }
//        }
        if (broker != null) {
            try {
                broker.stop();
                broker.waitUntilStopped();
            } catch (Exception e) {
            }
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
            pentry.setExpireMessagesPeriod(amqExpireMessagesPeriodSec * 1000);
            OldestMessageWithLowestPriorityEvictionStrategy eviction = new OldestMessageWithLowestPriorityEvictionStrategy();
            eviction.setEvictExpiredMessagesHighWatermark(amqExpiredMessagesHighWatermark);
            pentry.setMessageEvictionStrategy(eviction);
            ConstantPendingMessageLimitStrategy limit = new ConstantPendingMessageLimitStrategy();
            limit.setLimit(amqConstantPendingMessageLimit);
            pentry.setPendingMessageLimitStrategy(limit);
            policy.setDefaultEntry(pentry);
            broker.setOfflineDurableSubscriberTimeout(amqOfflineDurableSubscriberTimeoutSec * 1000);
            broker.setOfflineDurableSubscriberTaskSchedule(amqOfflineDurableSubscriberTaskScheduleSec * 1000);
            broker.setDestinationPolicy(policy);
            //***policy
            broker.setPersistent(true);
            broker.setUseJmx(false);
            broker.addConnector("ssl://" + this.amqBindAddress + ":" + this.amqPort);
            broker.start();
            broker.waitUntilStarted();
            //executor service setup
            exec = Executors.newFixedThreadPool(amqExecutorPoolSize);
            //pooled connetion factory setup
//            pooledConnectionFactory = new PooledConnectionFactory(new ActiveMQConnectionFactory("vm://localhost"));
//            pooledConnectionFactory.setMaxConnections(amqMaxConnections);
//            pooledConnectionFactory.start();
            connectionFactory = new ActiveMQConnectionFactory("vm://localhost");
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
        try {
//            Connection connection = pooledConnectionFactory.createConnection();
            Connection connection = connectionFactory.createConnection();
            //don not close this connection to not return it to the pool so no consumers are closed
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination adminQueue = session.createQueue(this.amqServiceQueue);
            MessageConsumer consumer = session.createConsumer(adminQueue);
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
