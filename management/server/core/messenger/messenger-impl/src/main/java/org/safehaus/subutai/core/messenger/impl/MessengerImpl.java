package org.safehaus.subutai.core.messenger.impl;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.messenger.api.Message;
import org.safehaus.subutai.core.messenger.api.MessageException;
import org.safehaus.subutai.core.messenger.api.MessageListener;
import org.safehaus.subutai.core.messenger.api.MessageProcessor;
import org.safehaus.subutai.core.messenger.api.MessageStatus;
import org.safehaus.subutai.core.messenger.api.Messenger;
import org.safehaus.subutai.core.messenger.api.MessengerException;
import org.safehaus.subutai.core.peer.api.Peer;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.gson.JsonSyntaxException;


/**
 * Implementation of Messenger
 */
public class MessengerImpl implements Messenger, MessageProcessor
{
    private static Logger LOG = LoggerFactory.getLogger( MessengerImpl.class.getName() );
    protected final Set<MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MessageListener, Boolean>() );
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    private final PeerManager peerManager;
    protected MessengerDao messengerDao;
    protected MessageSender messageSender;
    private EntityManagerFactory entityManagerFactory;


    public MessengerImpl( final DataSource dataSource, final PeerManager peerManager ) throws MessengerException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.peerManager = peerManager;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
        EntityManager entityManager = null;
        try
        {
            entityManager = entityManagerFactory.createEntityManager();
            this.messengerDao = new MessengerDao( entityManagerFactory );
            this.messageSender = new MessageSender( peerManager, messengerDao, this );
        }
        catch ( Exception ex )
        {
            LOG.error( "Error on creating entity manager.", ex );
        }
        finally
        {
            if ( entityManager != null )
            {
                entityManager.close();
            }
        }
    }


    public void init()
    {
        messageSender.init();
    }


    public void destroy()
    {
        messageSender.dispose();
        notificationExecutor.shutdown();
    }


    @Override
    public Message createMessage( final Object payload )
    {
        return new MessageImpl( peerManager.getLocalPeer().getId(), payload );
    }


    @Override
    public void sendMessage( final Peer peer, final Message message, final String recipient, final int timeToLive )
            throws MessageException
    {
        try
        {
            Envelope envelope = new Envelope( ( MessageImpl ) message, peer.getId(), recipient, timeToLive );

            messengerDao.saveEnvelope( envelope );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in sendMessage", e );
            throw new MessageException( e );
        }
    }


    @Override
    public MessageStatus getMessageStatus( final UUID messageId ) throws MessageException
    {

        try
        {
            Envelope envelope = messengerDao.getEnvelope( messageId );
            if ( envelope != null )
            {
                if ( envelope.isSent() )
                {
                    return MessageStatus.SENT;
                }
                //give 10 extra seconds in case background sender is in process of transmitting this message
                else if ( ( envelope.getCreateDate().getTime() + envelope.getTimeToLive() * 1000 )
                        < System.currentTimeMillis() + 10000 )
                {
                    return MessageStatus.IN_PROCESS;
                }
                else
                {
                    return MessageStatus.EXPIRED;
                }
            }

            return MessageStatus.NOT_FOUND;
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in getMessageStatus", e );
            throw new MessageException( e );
        }
    }


    @Override
    public void processMessage( String envelopeString ) throws MessageException
    {
        try
        {
            Envelope envelope = JsonUtil.fromJson( envelopeString, Envelope.class );
            notifyListeners( envelope );
        }
        catch ( NullPointerException | JsonSyntaxException e )
        {
            LOG.error( "Error in processMessage", e );
            throw new MessageException( e );
        }
    }


    protected void notifyListeners( Envelope envelope )
    {
        Message message = envelope.getMessage();

        for ( MessageListener listener : listeners )
        {
            if ( listener.getRecipient().equalsIgnoreCase( envelope.getRecipient() ) )
            {
                notificationExecutor.execute( new MessageNotifier( listener, message ) );
            }
        }
    }


    @Override
    public void addMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    @Override
    public void removeMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }
}
