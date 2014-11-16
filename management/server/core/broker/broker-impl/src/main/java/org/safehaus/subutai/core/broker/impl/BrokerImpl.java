package org.safehaus.subutai.core.broker.impl;


import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.safehaus.subutai.core.broker.api.Broker;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.MessageListener;
import org.safehaus.subutai.core.broker.api.TextMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;


/**
 * Broker implementation
 */
public class BrokerImpl implements Broker
{
    private static final Logger LOG = LoggerFactory.getLogger( BrokerImpl.class.getName() );

    protected MessageRoutingListener messageRouter;
    protected PooledConnectionFactory pool;
    private String brokerUrl;
    private int maxBrokerConnections;
    private boolean isPersistent;
    private int messageTimeout;


    public BrokerImpl( final String brokerUrl, final int maxBrokerConnections, final boolean isPersistent,
                       final int messageTimeout )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( brokerUrl ), "Invalid broker URL" );
        Preconditions.checkArgument( maxBrokerConnections > 0, "Max broker connections number must be greater than 0" );
        Preconditions.checkArgument( messageTimeout > 0, "Message timeout must be greater than 0" );

        this.brokerUrl = brokerUrl;
        this.maxBrokerConnections = maxBrokerConnections;
        this.isPersistent = isPersistent;
        this.messageTimeout = messageTimeout;
        this.messageRouter = new MessageRoutingListener();
    }


    @Override
    public void sendTextMessage( final String topic, final String message ) throws BrokerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topic ), "Invalid topic" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is empty" );

        sendMessage( topic, message );
    }


    @Override
    public void sendByteMessage( final String topic, final byte[] message ) throws BrokerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topic ), "Invalid topic" );
        Preconditions.checkArgument( message != null && message.length > 0, "Message is empty" );

        sendMessage( topic, message );
    }


    @Override
    public void addByteMessageListener( final ByteMessageListener listener ) throws BrokerException
    {
        Preconditions.checkNotNull( listener );

        messageRouter.addListener( listener );
    }


    @Override
    public void addTextMessageListener( final TextMessageListener listener ) throws BrokerException
    {
        Preconditions.checkNotNull( listener );

        messageRouter.addListener( listener );
    }


    @Override
    public void removeMessageListener( final MessageListener listener )
    {
        Preconditions.checkNotNull( listener );

        messageRouter.removeListener( listener );
    }


    public void init() throws BrokerException
    {
        setupConnectionPool();
        setupRouter();
    }


    public void dispose()
    {
        if ( pool != null )
        {
            pool.stop();
        }

        messageRouter.dispose();
    }


    protected void setupRouter() throws BrokerException
    {
        try
        {
            for ( Topic topic : Topic.values() )
            {
                Connection connection = pool.createConnection();
                connection.start();
                Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
                Destination topicDestination = session.createTopic( topic.name() );
                MessageConsumer consumer = session.createConsumer( topicDestination );
                consumer.setMessageListener( messageRouter );
            }
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in setupRouter", e );
            throw new BrokerException( e );
        }
    }


    private void setupConnectionPool()
    {
        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( brokerUrl );
        amqFactory.setCheckForDuplicates( true );
        pool = new PooledConnectionFactory( amqFactory );
        pool.setMaxConnections( maxBrokerConnections + Topic.values().length );
        pool.start();
    }


    private void sendMessage( String topic, Object message ) throws BrokerException
    {
        try
        {
            Connection connection = pool.createConnection();
            connection.start();
            Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination destination = session.createTopic( topic );
            MessageProducer producer = session.createProducer( destination );
            producer.setDeliveryMode( isPersistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT );
            producer.setTimeToLive( messageTimeout * 1000 );

            Message msg;
            if ( message instanceof String )
            {

                msg = session.createTextMessage( ( String ) message );
            }
            else
            {
                msg = session.createBytesMessage();
                ( ( BytesMessage ) msg ).writeBytes( ( byte[] ) message );
            }

            producer.send( msg );

            producer.close();
            session.close();
            connection.close();
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in sendMessage", e );
            throw new BrokerException( e );
        }
    }
}
