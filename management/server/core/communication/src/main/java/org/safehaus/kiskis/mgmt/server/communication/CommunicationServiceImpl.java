package org.safehaus.kiskis.mgmt.server.communication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandJson;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommunicationService;
import javax.jms.*;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.api.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;

public class CommunicationServiceImpl implements CommunicationService {

    private static final Logger LOG = Logger.getLogger(CommunicationServiceImpl.class.getName());
    private PooledConnectionFactory pooledConnectionFactory;
    private CommunicationMessageListener communicationMessageListener;
    private ExecutorService exec;
    private String amqServiceQueue;
    private int amqMaxMessageToAgentTtlSec;
    private int amqMaxPooledConnections;
    private int amqMaxSenderPoolSize;

    public void setAmqServiceQueue(String amqServiceQueue) {
        this.amqServiceQueue = amqServiceQueue;
    }

    public void setAmqMaxMessageToAgentTtlSec(int amqMaxMessageToAgentTtlSec) {
        this.amqMaxMessageToAgentTtlSec = amqMaxMessageToAgentTtlSec;
    }

    public void setAmqMaxPooledConnections(int amqMaxPooledConnections) {
        this.amqMaxPooledConnections = amqMaxPooledConnections;
    }

    public void setAmqMaxSenderPoolSize(int amqMaxSenderPoolSize) {
        this.amqMaxSenderPoolSize = amqMaxSenderPoolSize;
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
                connection = pooledConnectionFactory.createConnection();
                connection.start();
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                Destination destination = session.createQueue(command.getRequest().getUuid().toString());
                producer = session.createProducer(destination);
                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                producer.setTimeToLive(amqMaxMessageToAgentTtlSec * 1000);
                String json = CommandJson.getJson(command);
                if (command.getRequest().getType() != RequestType.HEARTBEAT_REQUEST) {
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

    public void init() {

        if (pooledConnectionFactory != null) {
            try {
                pooledConnectionFactory.stop();
            } catch (Exception e) {
            }
        }

        try {
            //executor service setup
            exec = Executors.newFixedThreadPool(amqMaxSenderPoolSize);
            //pooled connection factory setup
            ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory("vm://sol?create=false");
            amqFactory.setCheckForDuplicates(true);
            pooledConnectionFactory = new PooledConnectionFactory(amqFactory);
            pooledConnectionFactory.setMaxConnections(amqMaxPooledConnections);
            pooledConnectionFactory.start();
            setupListener();
            LOG.log(Level.INFO, "ActiveMQ started...");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in init", ex);
        }

    }

    public void destroy() {
        try {
            if (pooledConnectionFactory != null) {
                try {
                    pooledConnectionFactory.stop();
                } catch (Exception e) {
                }
            }
            if (communicationMessageListener != null) {
                try {
                    communicationMessageListener.destroy();
                } catch (Exception e) {
                }
            }
            LOG.log(Level.INFO, "ActiveMQ stopped...");
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in destroy", ex);
        }
    }

    private void setupListener() {
        try {
            Connection connection = pooledConnectionFactory.createConnection();
            //don not close this connection otherwise server listener will be closed
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination adminQueue = session.createQueue(this.amqServiceQueue);
            MessageConsumer consumer = session.createConsumer(adminQueue);
            communicationMessageListener = new CommunicationMessageListener();
            consumer.setMessageListener(communicationMessageListener);

            Destination advisoryDestination = AdvisorySupport.getProducerAdvisoryTopic(adminQueue);
            MessageConsumer advConsumer = session.createConsumer(advisoryDestination);
            advConsumer.setMessageListener(communicationMessageListener);
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error in setupListener", ex);
        }
    }

    @Override
    public void addListener(ResponseListener listener) {
        try {
            if (listener != null && communicationMessageListener != null) {
                communicationMessageListener.addListener(listener);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in addListener", ex);
        }
    }

    @Override
    public void removeListener(ResponseListener listener) {
        try {
            communicationMessageListener.removeListener(listener);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error in removeListener", ex);
        }
    }
}
