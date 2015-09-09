package io.subutai.core.broker.impl;


import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.jms.Connection;
import javax.jms.JMSException;
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
import org.apache.activemq.broker.region.policy.ConstantPendingMessageLimitStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.StrictOrderDispatchPolicy;
import org.apache.activemq.broker.util.TimeStampingBrokerPlugin;
import org.apache.activemq.network.ConditionalNetworkBridgeFilterFactory;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.security.crypto.certificate.CertificateData;
import io.subutai.core.broker.api.Broker;
import io.subutai.core.broker.api.BrokerException;
import io.subutai.core.broker.api.ByteMessageListener;
import io.subutai.core.broker.api.ByteMessagePostProcessor;
import io.subutai.core.broker.api.ByteMessagePreProcessor;
import io.subutai.core.broker.api.ClientCredentials;
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
    private final String keystore;
    private final String keystorePassword;
    private final String truststore;
    private final String truststorePassword;
    private final String caCertificate;
    private ByteMessagePostProcessor byteMessagePostProcessor;
    private TextMessagePostProcessor textMessagePostProcessor;
    private SslContext customSslContext;
    private ActiveMQConnectionFactory amqFactory;
    private SslUtil sslUtil = new SslUtil();
    protected ExecutorService messageSender = Executors.newFixedThreadPool( 5 );


    public BrokerImpl( final String brokerUrl, final String keystore, final String keystorePassword,
                       final String truststore, final String truststorePassword, final String caCertificate )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( brokerUrl ), "Invalid broker URL" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystore ), "Invalid keystore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystorePassword ), "Invalid keystore password" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststore ), "Invalid truststore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststorePassword ), "Invalid truststore password" );

        this.brokerUrl = brokerUrl;
        this.messageRouter = new MessageRoutingListener();
        //todo prefix store paths with Common.SUBUTAI_APP_DATA_PATH
        this.keystore = keystore;
        this.keystorePassword = keystorePassword;
        this.truststore = truststore;
        this.truststorePassword = truststorePassword;
        this.caCertificate = caCertificate;
    }


    @Override
    public ClientCredentials createNewClientCredentials( String clientId ) throws BrokerException
    {
        try
        {
            KeyPair keyPair = sslUtil.generateKeyPair( "RSA", 2048 );
            CertificateData certData = new CertificateData();
            certData.setCommonName( String.format( "client-%s", clientId ) );
            X509Certificate certificate = sslUtil.generateSelfSignedCertificate( keyPair, certData );
            String clientCert = sslUtil.convertToPem( certificate );
            String clientKey = sslUtil.convertToPem( keyPair.getPrivate() );
            String caCert = new String( Files.readAllBytes( Paths.get( caCertificate ) ) );
            //add client certificate to broker truststore
            registerClientCertificateWithBroker( String.format( "client-%s", clientId ), certificate );

            return new ClientCredentials( clientCert, clientKey, caCert );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in createNewClientCredentials", e );
            throw new BrokerException( e );
        }
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


    public synchronized void registerClientCertificateWithBroker( String alias, X509Certificate certificate )
            throws BrokerException
    {
        try
        {
            ReloadableX509TrustManager tm = ( ReloadableX509TrustManager ) getSslContext().getTrustManagersAsArray()[0];
            tm.addServerCertAndReload( alias, certificate );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in registerClientCertificateWithBroker", e );
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
            amqFactory.setUseAsyncSend( true );
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

            //tune broker
            tuneBroker();

            getBroker().addConnector( VM_BROKER_URL );
            getBroker().addConnector( brokerUrl );
            getBroker().start();
            getBroker().waitUntilStarted();

            addBrokerPlugins();
        }
        catch ( Exception e )
        {
            LOG.error( "Error in setupClient", e );
            throw new BrokerException( e );
        }
    }


    protected void addBrokerPlugins() throws Exception
    {
        /* TimeStampingBrokerPlugin handles the possible miss alignments among broker and producers
         * timestamps. Besides, it overides the zero-TTL specified by the producers
         */
        TimeStampingBrokerPlugin tsbp = getTimeStampingBrokerPlugin();
        tsbp.setZeroExpirationOverride( 200000 );

        tsbp.installPlugin( getBroker().getBroker() );
        tsbp.start();
    }


    protected TimeStampingBrokerPlugin getTimeStampingBrokerPlugin()
    {
        return new TimeStampingBrokerPlugin();
    }


    protected void tuneBroker() throws Exception
    {

            /* We want to delete destinations that are inactive for a period of time. Since ActiveMQ version 5.4.0,
             * it's possible to do that using destination policy entries and broker attribute
             * schedulePeriodForDestinationPurge > 0
             */
        getBroker().setSchedulePeriodForDestinationPurge( 300000 ); //5 minutes

            /* This is to set how the broker should dispatch outgoing messages:
             * At the time being, PolicyEntry items do not aggregate (see at
             * http://activemq.2283324.n4.nabble.com/Do-policy-map-entries-aggregate-tt4297601.html#a4300231)
             * There is a best match, and the default takes the unmatched case: all topics starting with
             * "stream." will use the stream_entry policy while all other topics will default on std_entry
             * policy.
             */
        PolicyMap map = new PolicyMap();
        PolicyEntry std_entry = new PolicyEntry();
        PolicyEntry stream_entry = new PolicyEntry();

        // All topics:
        std_entry.setTopic( ">" );
        stream_entry.setTopic( "stream.>" );
        std_entry.setDispatchPolicy( new StrictOrderDispatchPolicy() );

        std_entry.setOptimizedDispatch( true );
        stream_entry.setOptimizedDispatch( true );

        final long EXPIRE_MSG_PERIOD = 200000; //200 seconds
        final long STREAM_EXPIRE_MSG_PERIOD = 3000; //3 seconds

            /* Sets the strategy to calculate the maximum number of messages that are allowed
             * to be pending on consumers (in addition to their prefetch sizes).
             * Once the limit is reached, non-durable topics can then start discarding old
             * messages. This allows us to keep dispatching messages to slow consumers while
             * not blocking fast consumers and discarding the messages oldest first.
             */
        final long PENDING_MSG_LIMIT = 200000; //after this, start check TTL
        ConstantPendingMessageLimitStrategy pendMsgStrategy = new ConstantPendingMessageLimitStrategy();
        pendMsgStrategy.setLimit( ( int ) PENDING_MSG_LIMIT );
        std_entry.setPendingMessageLimitStrategy( pendMsgStrategy );

        final long STREAM_PENDING_MSG_LIMIT = 200000; //after this, start check TTL
        ConstantPendingMessageLimitStrategy streamPendMsgStrategy = new ConstantPendingMessageLimitStrategy();
        streamPendMsgStrategy.setLimit( ( int ) STREAM_PENDING_MSG_LIMIT );
        stream_entry.setPendingMessageLimitStrategy( streamPendMsgStrategy );

            /* See: http://activemq.apache.org/producer-flow-control.html
             * With producer flow control disabled, messages for slow consumers will be off-lined to
             * temporary storage by default, enabling the producers and the rest of the consumers to
             * run  at  a  much  faster  rate:
             */
        std_entry.setProducerFlowControl( false );

            /* See http://activemq.apache.org/manage-durable-subscribers.html
             * Some applications send message with specified time to live. If those messages are kept on
             * the broker for the offline durable subscriber we need to remove them when they reach their
             * expiry time. Just as AMQ does with queues, now AMQ checks for those messages every
             * EXPIRE_MSG_PERIOD.
             * This configuration complements the Timestampplugin (http://activemq.apache.org/timestampplugin.html)
             */
        std_entry.setExpireMessagesPeriod( ( int ) EXPIRE_MSG_PERIOD );
        stream_entry.setProducerFlowControl( false );
        stream_entry.setExpireMessagesPeriod( ( int ) STREAM_EXPIRE_MSG_PERIOD );

            /* This will check for inactive destination and it will delete all queues (gcInactiveDestinations option)
             * if they are empty for "OfflineDurableSubscriberTimeout" millis
             */
        std_entry.setGcInactiveDestinations( true );
        std_entry.setInactiveTimeoutBeforeGC( 1200000 );
        stream_entry.setGcInactiveDestinations( true );
        stream_entry.setInactiveTimeoutBeforeGC( 1200000 );

        map.setDefaultEntry( std_entry );

        LinkedList<PolicyEntry> policies = new LinkedList<>();
        policies.add( stream_entry );
        map.setPolicyEntries( policies );

        getBroker().setDestinationPolicy( map );

        // Set memory manager: refer to the following links for further documentation:
        // - http://activemq.2283324.n4.nabble.com/StoreUsage-TempUsage-and-MemoryUsage-td2356734.html
        // And also (NOT OFFIAL but rather clear explanations):
        // - http://tmielke.blogspot.it/2011/02/observations-on-activemqs-temp-storage.html
        // - http://java.dzone.com/articles/activemq-understanding-memory
        // Default values (see http://activemq.apache.org/producer-flow-control.html#ProducerFlowControl-Systemusage):
        //      - Default Memory limit is 64 mb
        //      - Default Temp Usage limit is 100 gb
        //      - Default Store Usage limit is 10 gb
        SystemUsage usage = getBroker().getSystemUsage();

        long memLimit = 1024L * 1024L,
                tempLimit = 1024L * 1024L,
                storeLimit = 1024L * 1024L;

        memLimit *= 64;
        tempLimit *= 10000;
        storeLimit *= 50000;

        org.apache.activemq.usage.MemoryUsage memUsage = usage.getMemoryUsage();
        memUsage.setLimit( memLimit );
        usage.setMemoryUsage( memUsage );

        TempUsage tmpUsage = usage.getTempUsage();
        tmpUsage.setLimit( tempLimit );
        usage.setTempUsage( tmpUsage );

        StoreUsage storeUsage = usage.getStoreUsage();
        storeUsage.setLimit( storeLimit );
        usage.setStoreUsage( storeUsage );


        usage.setSendFailIfNoSpace( true );


        getBroker().setSystemUsage( usage );

        PolicyEntry std_entry2 = new PolicyEntry();
        std_entry2.setTopic( ">" );
        std_entry2.setEnableAudit( false );
        ConditionalNetworkBridgeFilterFactory bff = new ConditionalNetworkBridgeFilterFactory();
        bff.setReplayWhenNoConsumers( true );
        std_entry2.setNetworkBridgeFilterFactory( bff );

        LinkedList<PolicyEntry> policies2 = new LinkedList<>();
        policies2.add( std_entry2 );
        getBroker().getDestinationPolicy().setPolicyEntries( policies2 );
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


    protected void sendMessage( String topic, Object message ) throws BrokerException
    {
        messageSender.execute( new SendMessageTask( getConnectionFactory(), topic, message ) );
    }


    public void dispose() throws Exception
    {
        if ( broker != null )
        {
            broker.stop();
        }

        messageSender.shutdown();
    }
}
