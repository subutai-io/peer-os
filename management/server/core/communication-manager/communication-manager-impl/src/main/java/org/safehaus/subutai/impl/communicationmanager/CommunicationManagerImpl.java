package org.safehaus.subutai.impl.communicationmanager;


import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.safehaus.subutai.api.communicationmanager.CommunicationManager;
import org.safehaus.subutai.api.communicationmanager.ResponseListener;
import org.safehaus.subutai.shared.protocol.Request;
import org.safehaus.subutai.shared.protocol.settings.Common;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.advisory.AdvisorySupport;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.AbortSlowAckConsumerStrategy;
import org.apache.activemq.broker.region.policy.DeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.SharedDeadLetterStrategy;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * This class is implementation of Communication Manager.
 */
public class CommunicationManagerImpl implements CommunicationManager {

    private static final Logger LOG = Logger.getLogger( CommunicationManagerImpl.class.getName() );
    /**
     * broker
     */
    private BrokerService broker;
    /**
     * pooled connection factory to hold connection to broker
     */
    private PooledConnectionFactory pooledConnectionFactory;
    /**
     * message listener to receive messages from broker
     */
    private CommunicationMessageListener communicationMessageListener;
    /**
     * executor used to send requests to agents
     */
    private ExecutorService exec;
    /**
     * bind address
     */
    private String amqBindAddress;
    /**
     * service queue to listen on responses from agents
     */
    private String amqServiceQueue;
    /**
     * ssl certificate name
     */
    private String amqBrokerCertificateName;
    /**
     * ssl truststore name
     */
    private String amqBrokerTrustStoreName;

    /**
     * ssl certificate password
     */
    private String amqBrokerCertificatePwd;
    /**
     * ssl truststore password
     */
    private String amqBrokerTrustStorePwd;
    /**
     * broker port
     */
    private int amqPort;
    /**
     * ttl of message from server to agent
     */
    private int amqMaxMessageToAgentTtlSec;
    /**
     * ttl for offline agents after which they get evicted
     */
    private int amqMaxOfflineAgentTtlSec;
    /**
     * ttl for slow acking agents after which they get evicted
     */
    private int amqMaxSlowAgentConnectionTtlSec;
    /**
     * size of connection pool to broker
     */
    private int amqMaxPooledConnections;
    /**
     * size of executor pool to send request to agents
     */
    private int amqMaxSenderPoolSize;

    /**
     * timeout to drop inactive queues
     */
    private int amqInactiveQueuesDropTimeoutSec;


    public Connection createConnection() throws JMSException {
        return pooledConnectionFactory.createConnection();
    }


    int getAmqMaxMessageToAgentTtlSec() {
        return amqMaxMessageToAgentTtlSec;
    }


    public void setAmqMaxMessageToAgentTtlSec( int amqMaxMessageToAgentTtlSec ) {
        this.amqMaxMessageToAgentTtlSec = amqMaxMessageToAgentTtlSec;
    }


    public void setAmqPort( int amqPort ) {
        this.amqPort = amqPort;
    }


    public void setAmqBindAddress( String amqBindAddress ) {
        this.amqBindAddress = amqBindAddress;
    }


    public void setAmqServiceQueue( String amqServiceQueue ) {
        this.amqServiceQueue = amqServiceQueue;
    }


    public void setAmqBrokerCertificateName( String amqBrokerCertificateName ) {
        this.amqBrokerCertificateName = amqBrokerCertificateName;
    }


    public void setAmqBrokerTrustStoreName( String amqBrokerTrustStoreName ) {
        this.amqBrokerTrustStoreName = amqBrokerTrustStoreName;
    }


    public void setAmqBrokerCertificatePwd( String amqBrokerCertificatePwd ) {
        this.amqBrokerCertificatePwd = amqBrokerCertificatePwd;
    }


    public void setAmqBrokerTrustStorePwd( String amqBrokerTrustStorePwd ) {
        this.amqBrokerTrustStorePwd = amqBrokerTrustStorePwd;
    }


    public void setAmqMaxOfflineAgentTtlSec( int amqMaxOfflineAgentTtlSec ) {
        this.amqMaxOfflineAgentTtlSec = amqMaxOfflineAgentTtlSec;
    }


    public void setAmqMaxSlowAgentConnectionTtlSec( int amqMaxSlowAgentConnectionTtlSec ) {
        this.amqMaxSlowAgentConnectionTtlSec = amqMaxSlowAgentConnectionTtlSec;
    }


    public void setAmqMaxPooledConnections( int amqMaxPooledConnections ) {
        this.amqMaxPooledConnections = amqMaxPooledConnections;
    }


    public void setAmqMaxSenderPoolSize( int amqMaxSenderPoolSize ) {
        this.amqMaxSenderPoolSize = amqMaxSenderPoolSize;
    }


    public void setAmqInactiveQueuesDropTimeoutSec( int amqInactiveQueuesDropTimeoutSec ) {
        this.amqInactiveQueuesDropTimeoutSec = amqInactiveQueuesDropTimeoutSec;
    }


    /**
     * Sends request to agent
     *
     * @param request - request to send
     */
    public void sendRequest( Request request ) {
        exec.submit( new CommandProducer( request, this ) );
    }


    /**
     * Adds listener
     *
     * @param listener - listener to add
     */
    @Override
    public void addListener( ResponseListener listener ) {
        try {
            if ( listener != null && communicationMessageListener != null ) {
                communicationMessageListener.addListener( listener );
            }
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in addListener", ex );
        }
    }


    /**
     * Removes listener
     *
     * @param listener - - listener to remove
     */
    @Override
    public void removeListener( ResponseListener listener ) {
        try {
            if ( listener != null && communicationMessageListener != null ) {
                communicationMessageListener.removeListener( listener );
            }
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in removeListener", ex );
        }
    }


    public boolean isBrokerStarted() {
        return broker.isStarted();
    }


    public Collection getListeners() {
        return communicationMessageListener.getListeners();
    }


    /**
     * Initialized communication manager
     */
    public void init() {

        Preconditions.checkArgument( !Strings.isNullOrEmpty( amqBindAddress ), "Bind address is null or empty" );
        Preconditions.checkArgument( amqBindAddress.matches( Common.HOSTNAME_REGEX ), "Invalid bind address" );
        Preconditions.checkArgument( amqPort >= 1024 && amqPort <= 65536, "Port must be in range 1024 and 65536" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( amqServiceQueue ), "Service queue name is null or empty" );
        Preconditions.checkArgument( amqMaxPooledConnections >= 1, "Max Pool Connections size must be greater than 0" );
        Preconditions.checkArgument( amqMaxSenderPoolSize >= 1, "Max Sender Pool size must be greater than 0" );

        if ( pooledConnectionFactory != null ) {
            try {
                pooledConnectionFactory.stop();
            }
            catch ( Exception e ) {
            }
        }

        if ( broker != null ) {
            try {
                broker.stop();
                broker.waitUntilStopped();
            }
            catch ( Exception e ) {
            }
        }
        if ( communicationMessageListener != null ) {
            try {
                communicationMessageListener.destroy();
            }
            catch ( Exception e ) {
            }
        }

        try {
            System.setProperty( "javax.net.ssl.keyStore",
                    System.getProperty( "karaf.base" ) + "/certs/" + this.amqBrokerCertificateName );
            System.setProperty( "javax.net.ssl.keyStorePassword", this.amqBrokerCertificatePwd );
            System.setProperty( "javax.net.ssl.trustStore",
                    System.getProperty( "karaf.base" ) + "/certs/" + this.amqBrokerTrustStoreName );
            System.setProperty( "javax.net.ssl.trustStorePassword", this.amqBrokerTrustStorePwd );

            broker = new BrokerService();
            //***policy
            PolicyMap policy = new PolicyMap();
            PolicyEntry allDestinationsPolicyEntry = new PolicyEntry();
            //abort consumers not acking message within this period of time
            AbortSlowAckConsumerStrategy slowConsumerStrategy = new AbortSlowAckConsumerStrategy();
            slowConsumerStrategy.setMaxTimeSinceLastAck( amqMaxSlowAgentConnectionTtlSec * 1000 );
            allDestinationsPolicyEntry.setSlowConsumerStrategy( slowConsumerStrategy );
            //drop expired messages instead of sending to DLQ
            DeadLetterStrategy deadLetterStrategy = new SharedDeadLetterStrategy();
            deadLetterStrategy.setProcessExpired( false );
            allDestinationsPolicyEntry.setDeadLetterStrategy( deadLetterStrategy );
            //drop queues inactive fo this period of time
            allDestinationsPolicyEntry.setGcInactiveDestinations( true );
            allDestinationsPolicyEntry.setInactiveTimoutBeforeGC( amqInactiveQueuesDropTimeoutSec * 1000 );
            broker.setSchedulePeriodForDestinationPurge( 30000 );
            //
            policy.setDefaultEntry( allDestinationsPolicyEntry );
            //unsubscribe durable subscribers that are offline for this amount of time
            broker.setOfflineDurableSubscriberTimeout( amqMaxOfflineAgentTtlSec * 1000 );
            //
            broker.setDestinationPolicy( policy );
            //***policy
            broker.setPersistent( true );
            broker.setUseJmx( false );
            broker.addConnector(
                    "mqtt+nio+ssl://" + this.amqBindAddress + ":" + this.amqPort + "?needClientAuth=true" );
            broker.start();
            broker.waitUntilStarted();
            //executor service setup
            exec = Executors.newFixedThreadPool( amqMaxSenderPoolSize );
            //pooled connection factory setup
            ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( "vm://localhost?create=false" );
            amqFactory.setCheckForDuplicates( true );
            pooledConnectionFactory = new PooledConnectionFactory( amqFactory );
            pooledConnectionFactory.setMaxConnections( amqMaxPooledConnections );
            pooledConnectionFactory.start();
            setupListener();
            LOG.log( Level.INFO, "ActiveMQ started..." );
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in init", ex );
        }
    }


    /**
     * Sets up listener to receive messages from agents
     */
    private void setupListener() {
        try {
            Connection connection = pooledConnectionFactory.createConnection();
            //don not close this connection otherwise server listener will be closed
            connection.start();
            Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination adminQueue = session.createTopic( this.amqServiceQueue );
            MessageConsumer consumer = session.createConsumer( adminQueue );
            communicationMessageListener = new CommunicationMessageListener();
            consumer.setMessageListener( communicationMessageListener );

            Destination advisoryDestination = AdvisorySupport.getProducerAdvisoryTopic( adminQueue );
            MessageConsumer advConsumer = session.createConsumer( advisoryDestination );
            advConsumer.setMessageListener( communicationMessageListener );
        }
        catch ( JMSException ex ) {
            LOG.log( Level.SEVERE, "Error in setupListener", ex );
        }
    }


    /**
     * Disposes communcation manager
     */
    public void destroy() {
        try {
            if ( pooledConnectionFactory != null ) {
                try {
                    pooledConnectionFactory.stop();
                }
                catch ( Exception e ) {
                }
            }
            if ( broker != null ) {
                try {
                    broker.stop();
                }
                catch ( Exception e ) {
                }
            }
            if ( communicationMessageListener != null ) {
                try {
                    communicationMessageListener.destroy();
                }
                catch ( Exception e ) {
                }
            }
            exec.shutdown();

            LOG.log( Level.INFO, "ActiveMQ stopped..." );
        }
        catch ( Exception ex ) {
            LOG.log( Level.SEVERE, "Error in destroy", ex );
        }
    }
}
