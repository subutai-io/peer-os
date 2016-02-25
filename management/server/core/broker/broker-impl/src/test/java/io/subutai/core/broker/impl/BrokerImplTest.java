package io.subutai.core.broker.impl;


import java.io.File;
import java.io.PrintStream;
import java.util.concurrent.ExecutorService;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSubscriber;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.util.TimeStampingBrokerPlugin;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;

import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.TextMessageListener;
import io.subutai.core.broker.api.Topic;

import static junit.framework.TestCase.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class BrokerImplTest
{

    private static final String BROKER_URL = "vm://localhost";
    private static final String KEYSTORE = "path/to/keystore";
    private static final String KEYSTORE_PASSWORD = "pwd";
    private static final String TOPIC = "topic";
    private static final String TEXT_MESSAGE = "message";
    private static final byte[] BYTE_MESSAGE = { 0 };
    @Mock
    MessageRoutingListener messageRouter;

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
    @Mock
    SslContext sslContext;
    @Mock
    BrokerService brokerService;
    @Mock
    SystemUsage systemUsage;
    @Mock
    MemoryUsage memoryUsage;
    @Mock
    TempUsage tempUsage;
    @Mock
    StoreUsage storeUsage;
    @Mock
    PolicyMap policyMap;
    @Mock
    TimeStampingBrokerPlugin timeStampingBrokerPlugin;
    @Mock
    ExecutorService messageSender;
    @Mock
    File mockDbFile;

    BrokerImpl broker;


    @Before
    public void setUp() throws Exception
    {
        broker = spy( new BrokerImpl( BROKER_URL, KEYSTORE, KEYSTORE_PASSWORD, KEYSTORE, KEYSTORE_PASSWORD ) );
        broker.messageSender = messageSender;
        doReturn( systemUsage ).when( brokerService ).getSystemUsage();
        doReturn( memoryUsage ).when( systemUsage ).getMemoryUsage();
        doReturn( tempUsage ).when( systemUsage ).getTempUsage();
        doReturn( storeUsage ).when( systemUsage ).getStoreUsage();
        doReturn( policyMap ).when( brokerService ).getDestinationPolicy();
        doReturn( sslContext ).when( broker ).getSslContext();
        doReturn( brokerService ).when( broker ).getBroker();
        doReturn( amqFactory ).when( broker ).getConnectionFactory();
        doReturn( timeStampingBrokerPlugin ).when( broker ).getTimeStampingBrokerPlugin();
        when( amqFactory.createConnection() ).thenReturn( connection );
        when( connection.createSession( anyBoolean(), anyInt() ) ).thenReturn( session );
        when( session.createProducer( any( Destination.class ) ) ).thenReturn( producer );
        when( session.createConsumer( any( Destination.class ) ) ).thenReturn( consumer );
        when( session.createBytesMessage() ).thenReturn( mock( BytesMessage.class ) );
        when( byteMessageListener.getTopic() ).thenReturn( Topic.RESPONSE_TOPIC );
        when( textMessageListener.getTopic() ).thenReturn( Topic.RESPONSE_TOPIC );
        broker.messageRouter = messageRouter;
        broker.broker = brokerService;
        doReturn( topicSubscriber ).when( session )
                                   .createDurableSubscriber( any( javax.jms.Topic.class ), anyString() );
    }


    @Test
    public void testSendTextMessage() throws Exception
    {
        broker.sendTextMessage( TOPIC, TEXT_MESSAGE );

        messageSender.execute( isA( SendMessageTask.class ) );
    }


    @Test
    public void testSendByteMessage() throws Exception
    {
        broker.sendByteMessage( TOPIC, BYTE_MESSAGE );

        messageSender.execute( isA( SendMessageTask.class ) );
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

        verify( brokerService ).stop();
    }


    @Test
    public void testInit() throws Exception
    {
        doReturn( mockDbFile ).when( broker ).getBrokerDbPath();
        broker.init();
    }


    @Test
    public void testSetupClient() throws Exception
    {
        when( amqFactory.createConnection() ).thenReturn( connection );


        broker.setupClient();

        verify( connection, times( Topic.values().length ) ).createSession( anyBoolean(), anyInt() );
        verify( topicSubscriber, times( Topic.values().length ) ).setMessageListener( messageRouter );

        JMSException exception = mock( JMSException.class );
        doThrow( exception ).when( amqFactory ).createConnection();

        try
        {
            broker.setupClient();
            fail( "Expected BrokerException" );
        }
        catch ( BrokerException e )
        {
        }
        verify( exception ).printStackTrace( any( PrintStream.class ) );
    }
}
