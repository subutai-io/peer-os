package io.subutai.core.messenger.impl;


import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gson.JsonSyntaxException;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.peer.Peer;
import io.subutai.common.util.JsonUtil;
import io.subutai.common.util.ServiceLocator;
import io.subutai.core.messenger.api.Message;
import io.subutai.core.messenger.api.MessageException;
import io.subutai.core.messenger.api.MessageListener;
import io.subutai.core.messenger.api.MessageProcessor;
import io.subutai.core.messenger.api.MessageStatus;
import io.subutai.core.messenger.api.Messenger;
import io.subutai.core.messenger.api.MessengerException;
import io.subutai.core.peer.api.PeerManager;


/**
 * Implementation of Messenger
 */
public class MessengerImpl implements Messenger, MessageProcessor
{
    private static Logger LOG = LoggerFactory.getLogger( MessengerImpl.class.getName() );
    protected final Set<MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MessageListener, Boolean>() );
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    protected MessengerDao messengerDao;
    protected MessageSender messageSender;
    private DaoManager daoManager;


    public void init() throws MessengerException
    {
        Preconditions.checkNotNull( daoManager );

        try
        {
            this.messengerDao = new MessengerDao( daoManager.getEntityManagerFactory() );
            this.messageSender = new MessageSender( messengerDao, this );

            messageSender.init();
        }
        catch ( Exception e )
        {
            LOG.error( "Error on creating entity manager.", e );
            throw new MessengerException( e );
        }
    }


    public void destroy()
    {
        messageSender.dispose();
        notificationExecutor.shutdown();
    }


    public void setDaoManager( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public Message createMessage( final Object payload )
    {
        Preconditions.checkNotNull( payload, "Invalid payload" );

        return new MessageImpl( getPeerManager().getLocalPeer().getId(), payload );
    }


    protected PeerManager getPeerManager()
    {
        try
        {
            return ServiceLocator.getServiceNoCache( PeerManager.class );
        }
        catch ( NamingException e )
        {
            throw new RuntimeException( "Failed to obtain PeerManager service", e );
        }
    }


    @Override
    public void sendMessage( final Peer peer, final Message message, final String recipient, final int timeToLive,
                             final Map<String, String> headers ) throws MessageException
    {
        Preconditions.checkNotNull( peer, "Peer is null" );
        Preconditions.checkNotNull( message, "Message is null" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( recipient ), "Invalid recipient" );
        Preconditions.checkArgument( timeToLive > 0, "Invalid time-to-live" );

        try
        {
            Envelope envelope = new Envelope( message, peer.getId(), recipient, timeToLive, headers );

            messengerDao.saveEnvelope( envelope );
        }
        catch ( Exception e )
        {
            LOG.error( "Error in sendMessage", e );
            throw new MessageException( e );
        }
    }


    @Override
    public MessageStatus getMessageStatus( final UUID messageId ) throws MessageException
    {
        Preconditions.checkNotNull( messageId, "Invalid message id" );

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
                else if ( ( envelope.getCreateDate().getTime() + ( envelope.getTimeToLive() + 10 ) * 1000 ) > System
                        .currentTimeMillis() )
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
        catch ( Exception e )
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


    public void addMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {

            listeners.add( listener );
        }
    }


    public void removeMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }
}
