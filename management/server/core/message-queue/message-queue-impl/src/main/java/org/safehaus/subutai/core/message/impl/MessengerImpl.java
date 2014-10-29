package org.safehaus.subutai.core.message.impl;


import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.safehaus.subutai.core.message.api.MessageProcessor;
import org.safehaus.subutai.core.message.api.MessageStatus;
import org.safehaus.subutai.core.message.api.Messenger;
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
    private static final Logger LOG = LoggerFactory.getLogger( MessengerImpl.class.getName() );
    private final Set<MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MessageListener, Boolean>() );
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    private final PeerManager peerManager;
    protected MessengerDao messengerDao;
    protected MessageSender messageSender;


    public MessengerImpl( final PeerManager peerManager, final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( peerManager, "Peer Manager is null" );
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.peerManager = peerManager;
        this.messengerDao = new MessengerDao( dataSource );
        this.messageSender = new MessageSender( peerManager, messengerDao );
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
    public Message createMessage( final Object payload ) throws MessageException
    {
        return new MessageImpl( peerManager.getPeerId(), payload );
    }


    @Override
    public void sendMessage( final Peer peer, final Message message, final String recipient,
                             final int timeToLive ) throws MessageException
    {
        try
        {
            //TODO use commented code to create Envelope when peer and peerManager are completed
            //            Envelope envelope =
            //                    new Envelope( message, peerManager.getLocalPeer().getId(), peer.getId(), recipient,
            // timeToLive )
            Envelope envelope = new Envelope( ( MessageImpl ) message, peerManager.getPeerId(), recipient, timeToLive );

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
                else
                {
                    //give 10 extra seconds in case background sender is in process of transmitting this message
                    if ( ( envelope.getCreateDate().getTime() + envelope.getTimeToLive() * 1000 )
                            < System.currentTimeMillis() + 10 * 1000 )
                    {
                        return MessageStatus.IN_PROCESS;
                    }
                    else
                    {
                        return MessageStatus.EXPIRED;
                    }
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
            Message message = envelope.getMessage();

            for ( MessageListener listener : listeners )
            {
                if ( listener.getRecipient().equalsIgnoreCase( envelope.getRecipient() ) )
                {
                    notificationExecutor.execute( new MessageNotifier( listener, message ) );
                }
            }
        }
        catch ( JsonSyntaxException e )
        {
            LOG.error( "Error in processMessage", e );
            throw new MessageException( e );
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
