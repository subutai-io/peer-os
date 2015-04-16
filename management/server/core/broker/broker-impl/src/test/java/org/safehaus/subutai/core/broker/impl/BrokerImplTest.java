package org.safehaus.subutai.core.broker.impl;


import java.io.PrintStream;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.safehaus.subutai.core.broker.api.BrokerException;
import org.safehaus.subutai.core.broker.api.ByteMessageListener;
import org.safehaus.subutai.core.broker.api.TextMessageListener;
import org.safehaus.subutai.core.broker.api.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BrokerImplTest
{

    private static final int MAX_BROKER_CONNECTIONS = 1;
    private static final String BROKER_URL = "vm://localhost";
    private static final int MESSAGE_TIMEOUT = 10;
    private static final int IDLE_CONNECTION_TIMEOUT = 300;
    private static final String TOPIC = "topic";
    private static final String TEXT_MESSAGE = "message";
    private static final byte[] BYTE_MESSAGE = { 0 };
    @Mock
    MessageRoutingListener messageRouter;

    @Mock
    PooledConnectionFactory pool;

    @Mock
    Connection connection;

    @Mock
    Session session;

    @Mock
    MessageProducer producer;
    @Mock
    MessageConsumer consumer;

    @Mock
    ByteMessageListener byteMessageListener;

    @Mock
    TextMessageListener textMessageListener;

    @Mock
    ActiveMQConnectionFactory amqFactory;

    @Mock
    TopicSubscriber topicSubscriber;

    BrokerImpl broker;


    @Before
    public void setUp() throws Exception
    {
        broker = new BrokerImpl( BROKER_URL, MAX_BROKER_CONNECTIONS, true, MESSAGE_TIMEOUT, IDLE_CONNECTION_TIMEOUT );
        when( pool.createConnection() ).thenReturn( connection );
        when( connection.createSession( anyBoolean(), anyInt() ) ).thenReturn( session );
        when( session.createProducer( any( Destination.class ) ) ).thenReturn( producer );
        when( session.createConsumer( any( Destination.class ) ) ).thenReturn( consumer );
        when( session.createBytesMessage() ).thenReturn( mock( BytesMessage.class ) );
        when( byteMessageListener.getTopic() ).thenReturn( Topic.RESPONSE_TOPIC );
        when( textMessageListener.getTopic() ).thenReturn( Topic.RESPONSE_TOPIC );
        broker.pool = pool;
        broker.messageRouter = messageRouter;
    }


    @Test
    public void testConstructor() throws Exception
    {
        //test url
        try
        {
            new BrokerImpl( null, MAX_BROKER_CONNECTIONS, true, MESSAGE_TIMEOUT, IDLE_CONNECTION_TIMEOUT );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
        }
        //test max connections
        try
        {
            new BrokerImpl( BROKER_URL, -1, true, MESSAGE_TIMEOUT, IDLE_CONNECTION_TIMEOUT );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
        }
        //check message timeout
        try
        {
            new BrokerImpl( BROKER_URL, MAX_BROKER_CONNECTIONS, true, -1, IDLE_CONNECTION_TIMEOUT );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
        }        //check message timeout
        try
        {
            new BrokerImpl( BROKER_URL, MAX_BROKER_CONNECTIONS, true, MESSAGE_TIMEOUT, -1 );
            fail( "Expected IllegalArgumentException" );
        }
        catch ( IllegalArgumentException e )
        {
        }
    }


    @Test
    public void testSendTextMessage() throws Exception
    {
        broker.sendTextMessage( TOPIC, TEXT_MESSAGE );

        verify( session ).createTopic( TOPIC );
        verify( session ).createTextMessage( TEXT_MESSAGE );
        verify( producer ).send( any( Message.class ) );

        verify( producer ).close();
        verify( session ).close();
        verify( connection ).close();
    }


    @Test
    public void testSendByteMessage() throws Exception
    {
        broker.sendByteMessage( TOPIC, BYTE_MESSAGE );

        verify( session ).createTopic( TOPIC );
        verify( session ).createBytesMessage();
        verify( producer ).send( any( Message.class ) );
    }


    @Test( expected = BrokerException.class )
    public void testSendMessageException() throws Exception
    {
        doThrow( new JMSException( null ) ).when( pool ).createConnection();

        broker.sendTextMessage( TOPIC, TEXT_MESSAGE );
    }


    @Test
    public void testAddByteMessageListener() throws Exception
    {

        broker.addByteMessageListener( byteMessageListener );

        verify( messageRouter ).addListener( byteMessageListener );
    }


    @Test
    public void testAddTextMessageListener() throws Exception
    {
        broker.addTextMessageListener( textMessageListener );

        verify( messageRouter ).addListener( textMessageListener );
    }


    @Test
    public void testRemoveListener() throws Exception
    {

        broker.removeMessageListener( textMessageListener );

        verify( messageRouter ).removeListener( textMessageListener );
    }


    @Test
    public void testDispose() throws Exception
    {
        broker.dispose();

        verify( pool ).stop();
    }


    @Test
    public void testInit() throws Exception
    {
        broker.init();
    }


    @Test
    public void testSetupRouter() throws Exception
    {
        when( amqFactory.createConnection() ).thenReturn( connection );
        when( session.createDurableSubscriber( any( javax.jms.Topic.class ), anyString() ) )
                .thenReturn( topicSubscriber );

        broker.setupRouter( amqFactory );

        verify( connection, times( Topic.values().length ) ).createSession(anyBoolean(), anyInt());
        verify( topicSubscriber, times( Topic.values().length ) ).setMessageListener( messageRouter );

        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( amqFactory ).createConnection();

        try
        {
            broker.setupRouter( amqFactory );
            fail( "Expected BrokerException" );
        }
        catch ( BrokerException e )
        {
        }
        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
