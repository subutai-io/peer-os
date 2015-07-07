package io.subutai.core.broker.impl;


import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.MessageListener;
import io.subutai.core.broker.api.TextMessageListener;
import io.subutai.core.broker.api.Topic;
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
    private final String brokerUrl;
    private final int maxBrokerConnections;
    private final boolean isPersistent;
    private final int messageTimeout;
    private final int idleConnectionTimeout;


    public BrokerImpl( final String brokerUrl, final int maxBrokerConnections, final boolean isPersistent,
                       final int messageTimeout, final int idleConnectionTimeout )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( brokerUrl ), "Invalid broker URL" );
        Preconditions.checkArgument( maxBrokerConnections > 0, "Max broker connections number must be greater than 0" );
        Preconditions.checkArgument( messageTimeout >= 0, "Message timeout must be greater than or equal to 0" );
        Preconditions.checkArgument( idleConnectionTimeout > 0, "Idle connection timeout must be greater than 0" );

        this.brokerUrl = brokerUrl;
        this.maxBrokerConnections = maxBrokerConnections;
        this.isPersistent = isPersistent;
        this.messageTimeout = messageTimeout;
        this.idleConnectionTimeout = idleConnectionTimeout;
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


    public void addByteMessageListener( final ByteMessageListener listener ) throws BrokerException
    {
        Preconditions.checkNotNull( listener );
        Preconditions.checkNotNull( listener.getTopic() );

        messageRouter.addListener( listener );
    }


    public void addTextMessageListener( final TextMessageListener listener ) throws BrokerException
    {
        Preconditions.checkNotNull( listener );
        Preconditions.checkNotNull( listener.getTopic() );

        messageRouter.addListener( listener );
    }


    public void removeMessageListener( final MessageListener listener )
    {
        Preconditions.checkNotNull( listener );

        messageRouter.removeListener( listener );
    }


    public void init() throws BrokerException
    {
        ActiveMQConnectionFactory amqFactory = new ActiveMQConnectionFactory( brokerUrl );
        amqFactory.setWatchTopicAdvisories( false );
        amqFactory.setCheckForDuplicates( true );

        setupConnectionPool( amqFactory );
        setupRouter( amqFactory );
    }


    public void dispose()
    {
        if ( pool != null )
        {
            pool.stop();
        }
    }


    protected void setupRouter( ActiveMQConnectionFactory amqFactory ) throws BrokerException
    {
        try
        {
            for ( Topic topic : Topic.values() )
            {
                Connection connection = amqFactory.createConnection();
                connection.setClientID( String.format( "%s-subutai-client", topic.name() ) );
                connection.start();
                Session session = connection.createSession( false, Session.CLIENT_ACKNOWLEDGE );
                javax.jms.Topic topicDestination = session.createTopic( topic.name() );
                TopicSubscriber topicSubscriber = session.createDurableSubscriber( topicDestination,
                        String.format( "%s-subutai-subscriber", topic.name() ) );
                topicSubscriber.setMessageListener( messageRouter );
            }
        }
        catch ( JMSException e )
        {
            LOG.error( "Error in setupRouter", e );
            throw new BrokerException( e );
        }
    }


    private void setupConnectionPool( ActiveMQConnectionFactory amqFactory )
    {
        pool = new PooledConnectionFactory( amqFactory );
        pool.setMaxConnections( maxBrokerConnections );
        pool.setIdleTimeout( idleConnectionTimeout * 1000 );
        pool.start();
    }


    private void sendMessage( String topic, Object message ) throws BrokerException
    {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try
        {
            connection = pool.createConnection();
            connection.start();
            session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
            Destination destination = session.createTopic( topic );
            producer = session.createProducer( destination );
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
        }
        catch ( Exception e )
        {
            LOG.error( "Error in sendMessage", e );
            throw new BrokerException( e );
        }
        finally
        {
            if ( producer != null )
            {
                try
                {
                    producer.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }

            if ( session != null )
            {
                try
                {
                    session.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }

            if ( connection != null )
            {
                try
                {
                    connection.close();
                }
                catch ( JMSException e )
                {
                    //ignore
                }
            }
        }
    }
}
