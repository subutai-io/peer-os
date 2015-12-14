package io.subutai.core.broker.impl;


import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
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
import org.apache.activemq.broker.region.policy.VMPendingSubscriberMessageStoragePolicy;
import org.apache.activemq.broker.util.TimeStampingBrokerPlugin;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.usage.StoreUsage;
import org.apache.activemq.usage.SystemUsage;
import org.apache.activemq.usage.TempUsage;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.settings.Common;
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
    private final String keystore;
    private final String keystorePassword;
    private final String truststore;
    private final String truststorePassword;
    private ByteMessagePostProcessor byteMessagePostProcessor;
    private TextMessagePostProcessor textMessagePostProcessor;
    private SslContext customSslContext;
    private ActiveMQConnectionFactory amqFactory;
    private SslUtil sslUtil = new SslUtil();
    protected ExecutorService messageSender = Executors.newFixedThreadPool( 5 );


    public BrokerImpl( final String brokerUrl, final String keystore, final String keystorePassword,
                       final String truststore, final String truststorePassword )
    {
        Preconditions.checkArgument( !Strings.isNullOrEmpty( brokerUrl ), "Invalid broker URL" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystore ), "Invalid keystore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( keystorePassword ), "Invalid keystore password" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststore ), "Invalid truststore path" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( truststorePassword ), "Invalid truststore password" );

        this.brokerUrl = brokerUrl;
        this.messageRouter = new MessageRoutingListener();
        this.keystore = String.format( "%s/%s", Common.SUBUTAI_APP_CERTS_PATH, keystore );
        this.keystorePassword = keystorePassword;
        this.truststore = String.format( "%s/%s", Common.SUBUTAI_APP_CERTS_PATH, truststore );
        this.truststorePassword = truststorePassword;
    }


    @Override
    public void registerClientCertificate( final String clientId, final String clientX509CertInPem )
            throws BrokerException
    {
        try
        {
            ReloadableX509TrustManager tm = ( ReloadableX509TrustManager ) getSslContext().getTrustManagersAsArray()[0];
            tm.addServerCertAndReload( clientId, sslUtil.convertX509PemToCert( clientX509CertInPem ) );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in registerClientCertificate", e );
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
            amqFactory.setOptimizeAcknowledge( true );
            amqFactory.setAlwaysSessionAsync( false );
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
        tsbp.setZeroExpirationOverride( 300000 );

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
        getBroker().setSchedulePeriodForDestinationPurge( 5 * 60 * 1000 ); // 5 min

            /* This is to set how the broker should dispatch outgoing messages:
             * At the time being, PolicyEntry items do not aggregate (see at
             * http://activemq.2283324.n4.nabble.com/Do-policy-map-entries-aggregate-tt4297601.html#a4300231)
             * There is a best match, and the default takes the unmatched case: all topics starting with
             * "stream." will use the stream_entry policy while all other topics will default on topicPolicyEntry
             * policy.
             */
        PolicyMap map = new PolicyMap();
        PolicyEntry topicPolicyEntry = new PolicyEntry();

        // All topics:
        topicPolicyEntry.setTopic( ">" );
        topicPolicyEntry.setEnableAudit( false );
        topicPolicyEntry.setDurableTopicPrefetch( 10000 );
        topicPolicyEntry.setExpireMessagesPeriod( 4 * 60 * 1000 ); // 4 min
        topicPolicyEntry.setOptimizedDispatch( true );

        topicPolicyEntry.setPendingSubscriberPolicy( new VMPendingSubscriberMessageStoragePolicy() );

            /* Sets the strategy to calculate the maximum number of messages that are allowed
             * to be pending on consumers (in addition to their prefetch sizes).
             * Once the limit is reached, non-durable topics can then start discarding old
             * messages. This allows us to keep dispatching messages to slow consumers while
             * not blocking fast consumers and discarding the messages oldest first.
             */
        final long PENDING_MSG_LIMIT = 10000; //after this, start check TTL
        ConstantPendingMessageLimitStrategy pendMsgStrategy = new ConstantPendingMessageLimitStrategy();
        pendMsgStrategy.setLimit( ( int ) PENDING_MSG_LIMIT );
        topicPolicyEntry.setPendingMessageLimitStrategy( pendMsgStrategy );

            /* See: http://activemq.apache.org/producer-flow-control.html
             * With producer flow control disabled, messages for slow consumers will be off-lined to
             * temporary storage by default, enabling the producers and the rest of the consumers to
             * run  at  a  much  faster  rate:
             */
        topicPolicyEntry.setProducerFlowControl( false );

            /* This will check for inactive destination and it will delete all queues (gcInactiveDestinations option)
             * if they are empty for "OfflineDurableSubscriberTimeout" millis
             */
        topicPolicyEntry.setGcInactiveDestinations( true );
        topicPolicyEntry.setInactiveTimeoutBeforeGC( 20 * 60 * 1000 ); // 20 min

        map.setDefaultEntry( topicPolicyEntry );

        getBroker().setDestinationPolicy( map );

        SystemUsage usage = getBroker().getSystemUsage();

        long memLimit = 1024L * 1024L,
                tempLimit = 1024L * 1024L,
                storeLimit = 1024L * 1024L;

        memLimit *= 256; // 256 MB to store non persistent messages in memory
        tempLimit *= 64; // 64 MB to store temp messages in memory
        storeLimit *= 1024;// 1 GB to store persistent messages on disk

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

        KahaDBPersistenceAdapter kahaDBPersistenceAdapter = new KahaDBPersistenceAdapter();
        kahaDBPersistenceAdapter.setJournalMaxFileLength( 1024 * 1024 * 64 );// 64 MB
        kahaDBPersistenceAdapter.setConcurrentStoreAndDispatchTopics( true );
        kahaDBPersistenceAdapter.setIndexCacheSize( 15000 );
        kahaDBPersistenceAdapter.setIndexWriteBatchSize( 1500 );
        kahaDBPersistenceAdapter.setEnableJournalDiskSyncs( false );
        kahaDBPersistenceAdapter.setDirectory( getBrokerDbPath() );

        getBroker().setPersistenceAdapter( kahaDBPersistenceAdapter );
    }


    protected File getBrokerDbPath()
    {
        return new File( Common.SUBUTAI_APP_DATA_PATH + "/KahaDB" );
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
                Session session = connection.createSession( false, Session.AUTO_ACKNOWLEDGE );
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
