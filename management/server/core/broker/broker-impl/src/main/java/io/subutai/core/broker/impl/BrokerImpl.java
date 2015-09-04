package io.subutai.core.broker.impl;


import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TopicSubscriber;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.SslContext;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.ByteMessagePostProcessor;
import io.subutai.core.broker.api.ByteMessagePreProcessor;
import io.subutai.core.broker.api.MessageListener;
import io.subutai.core.broker.api.TextMessageListener;
import io.subutai.core.broker.api.TextMessagePostProcessor;
import io.subutai.core.broker.api.TextMessagePreProcessor;
import io.subutai.core.broker.api.Topic;


/**
 * Broker implementation
 */
public class BrokerImpl implements Broker
{
    private static final Logger LOG = LoggerFactory.getLogger( BrokerImpl.class.getName() );
    private static final String VM_BROKER_URL = "vm://localhost";

    protected MessageRoutingListener messageRouter;
    protected BrokerService broker;
    private final String brokerUrl;
    private final boolean isPersistent;
    private final int messageTimeout;
    private final String keystore;
    private final String keystorePassword;
    private final String truststore;
    private final String truststorePassword;
    private ByteMessagePostProcessor byteMessagePostProcessor;
    private TextMessagePostProcessor textMessagePostProcessor;
    private SslContext customSslContext;
    private ActiveMQConnectionFactory amqFactory;


    public BrokerImpl( final String brokerUrl, final boolean isPersistent, final int messageTimeout,
                       final String keystore, final String keystorePassword, final String truststore,
                       final String truststorePassword )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( brokerUrl ), "Invalid broker URL" );
        Preconditions.checkArgument( messageTimeout >= 0, "Message timeout must be greater than or equal to 0" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystore ), "Invalid keystore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystorePassword ), "Invalid keystore password" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststore ), "Invalid truststore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststorePassword ), "Invalid truststore password" );

        this.brokerUrl = brokerUrl;
        this.isPersistent = isPersistent;
        this.messageTimeout = messageTimeout;
        this.messageRouter = new MessageRoutingListener();
        //todo prefix store paths with Common.SUBUTAI_APP_DATA_PATH
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
        this.truststorePassword = truststorePassword;
    }


    @Override
    public void sendTextMessage( final String topic, String message ) throws BrokerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topic ), "Invalid topic" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( message ), "Message is empty" );

        //post-process the message
        if ( textMessagePostProcessor != null )
        {
            message = textMessagePostProcessor.process( topic, message );
        }

        sendMessage( topic, message );
    }


    @Override
    public void sendByteMessage( final String topic, byte[] message ) throws BrokerException
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( topic ), "Invalid topic" );
        Preconditions.checkArgument( message != null && message.length > 0, "Message is empty" );

        //post-process the message
        if ( byteMessagePostProcessor != null )
        {
            message = byteMessagePostProcessor.process( topic, message );
        }

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


    public void addByteMessagePreProcessor( final ByteMessagePreProcessor byteMessagePreProcessor )
    {
        if ( byteMessagePreProcessor != null )
        {
            messageRouter.addByteMessagePreProcessor( byteMessagePreProcessor );
        }
    }


    public void removeByteMessagePreProcessor( final ByteMessagePreProcessor byteMessagePreProcessor )
    {

        if ( byteMessagePreProcessor != null )
        {
            messageRouter.removeByteMessagePreProcessor( byteMessagePreProcessor );
        }
    }


    public void addTextMessagePreProcessor( final TextMessagePreProcessor textMessagePreProcessor )
    {
        if ( textMessagePreProcessor != null )
        {
            messageRouter.addTextMessagePreProcessor( textMessagePreProcessor );
        }
    }


    public void removeTextMessagePreProcessor( final TextMessagePreProcessor textMessagePreProcessor )
    {
        if ( textMessagePreProcessor != null )
        {
            messageRouter.removeTextMessagePreProcessor( textMessagePreProcessor );
        }
    }


    public void addByteMessagePostProcessor( final ByteMessagePostProcessor byteMessagePostProcessor )
    {
        if ( byteMessagePostProcessor != null )
        {
            this.byteMessagePostProcessor = byteMessagePostProcessor;
        }
    }


    public void removeByteMessagePostProcessor( final ByteMessagePostProcessor byteMessagePostProcessor )
    {
        if ( byteMessagePostProcessor != null )
        {
            this.byteMessagePostProcessor = null;
        }
    }


    public void addTextMessagePostProcessor( final TextMessagePostProcessor textMessagePostProcessor )
    {
        if ( textMessagePostProcessor != null )
        {
            this.textMessagePostProcessor = textMessagePostProcessor;
        }
    }


    public void removeTextMessagePostProcessor( final TextMessagePostProcessor textMessagePostProcessor )
    {
        if ( textMessagePostProcessor != null )
        {
            this.textMessagePostProcessor = null;
        }
    }


    public void removeMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            messageRouter.removeListener( listener );
        }
    }


    public synchronized void reloadTrustStore() throws BrokerException
    {
        try
        {
            ReloadableX509TrustManager tm = ( ReloadableX509TrustManager ) getSslContext().getTrustManagersAsArray()[0];
            tm.reloadTrustManager();
        }
        catch ( Exception e )
        {
            LOG.error( "Error reloading broker SSL context", e );
            throw new BrokerException( e );
        }
    }


    public void init() throws BrokerException
    {
        //setup broker
        setupBroker();

        //setup client

        setupClient();
    }


    protected BrokerService getBroker()
    {
        if ( broker == null )
        {
            broker = new BrokerService();
            //tune broker
        }
        return broker;
    }


    protected ActiveMQConnectionFactory getConnectionFactory()
    {
        if ( amqFactory == null )
        {
            amqFactory = new ActiveMQConnectionFactory( VM_BROKER_URL );
            //tune connection factory
            amqFactory.setWatchTopicAdvisories( false );
            amqFactory.setCheckForDuplicates( true );
        }
        return amqFactory;
    }


    protected SslContext getSslContext() throws Exception
    {
        if ( customSslContext == null )
        {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance( KeyManagerFactory.getDefaultAlgorithm() );
            KeyStore ks = KeyStore.getInstance( "jks" );
            KeyManager[] keystoreManagers;

            ks.load( new FileInputStream( new File( keystore ) ), keystorePassword.toCharArray() );
            kmf.init( ks, keystorePassword.toCharArray() );
            keystoreManagers = kmf.getKeyManagers();

            TrustManager[] trustStoreManagers = new TrustManager[] {
                    new ReloadableX509TrustManager( truststore, truststorePassword )
            };

            customSslContext = new SslContext( keystoreManagers, trustStoreManagers, null );
        }
        return customSslContext;
    }


    protected void setupBroker() throws BrokerException
    {
        try
        {
            getBroker().setSslContext( getSslContext() );
            getBroker().addConnector( VM_BROKER_URL );
            getBroker().addConnector( brokerUrl );
            getBroker().start();
            getBroker().waitUntilStarted();
        }
        catch ( Exception e )
        {
            LOG.error( "Error in setupClient", e );
            throw new BrokerException( e );
        }
    }


    protected void setupClient() throws BrokerException
    {
        try
        {
            for ( Topic topic : Topic.values() )
            {
                Connection connection = getConnectionFactory().createConnection();
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
            LOG.error( "Error in setupClient", e );
            throw new BrokerException( e );
        }
    }


    private void sendMessage( String topic, Object message ) throws BrokerException
    {
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;

        try
        {
            connection = getConnectionFactory().createConnection();
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


    public void dispose() throws Exception
    {
        if ( broker != null )
        {
            broker.stop();
        }
    }
}
