package org.safehaus.kiskis.mgmt.shared.communication;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.safehaus.kiskis.mgmt.shared.communication.impl.ServerMessageBroker;
import org.safehaus.kiskis.mgmt.shared.protocol.elements.Common;
import org.safehaus.kiskis.mgmt.shared.protocol.interfaces.server.RegisteredHostInterface;

import javax.jms.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * BundleActivator for mgmt-server bundle
 */
public class Activator implements BundleActivator {

    private static final Logger LOG = Logger.getLogger(Activator.class.getName());
    private BrokerService broker = new BrokerService();
    private static ServiceReference refServerBroker;
    private static BundleContext context;

    @Override
    public void start(BundleContext context) throws Exception {
        try {
            Activator.context = context;
            Activator.refServerBroker =
                    context.getServiceReference(RegisteredHostInterface.class.getName());

            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.addConnector("tcp://0.0.0.0:" + Common.MQ_PORT);
            broker.start();
            setupListener();
        } catch (Exception ex) {
            System.out.println(ex.toString());
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        try {
            broker.stop();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
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
            consumer.setMessageListener(new ServerMessageBroker(session));
            System.out.println("ActiveMQ started...");
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public static RegisteredHostInterface getServerBroker() {
        return ((RegisteredHostInterface)
                Activator.context.getService(Activator.refServerBroker));
    }
}
