package org.safehaus.subutai.core.message.impl;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.safehaus.subutai.core.message.api.MessageProcessor;
import org.safehaus.subutai.core.message.api.MessageStatus;
import org.safehaus.subutai.core.message.api.Queue;
import org.safehaus.subutai.core.peer.api.PeerInterface;
import org.safehaus.subutai.core.peer.api.PeerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


/**
 * Implementation of Queue
 */
public class QueueImpl implements Queue, MessageProcessor
{
    private static final Logger LOG = LoggerFactory.getLogger( QueueImpl.class.getName() );
    private final Set<MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MessageListener, Boolean>() );
    protected ExecutorService notificationExecutor = Executors.newCachedThreadPool();
    private final PeerManager peerManager;


    public QueueImpl( final PeerManager peerManager, final DataSource dataSource )
    {
        this.peerManager = peerManager;
    }


    @Override
    public Message createMessage( final Object payload ) throws MessageException
    {
        return new MessageImpl( payload );
    }


    @Override
    public void sendMessage( final PeerInterface peer, final Message message, final String recipient,
                             final int timeToLive ) throws MessageException
    {
        //TODO save to persistent queue so that background task will try to send it
        try
        {
            //            Envelope envelope =
            //                    new Envelope( message, peerManager.getLocalPeer().getId(), peer.getId(), recipient,
            // timeToLive )
            Envelope envelope = new Envelope( ( MessageImpl ) message, UUID.randomUUID(), UUID.randomUUID(), recipient,
                    timeToLive );

            Map<String, String> params = new HashMap<>();
            params.put( "envelope", JsonUtil.toJson( envelope ) );
            RestUtil.post( "http://172.16.131.203:8181/cxf/queue/message", params );
        }
        catch ( HTTPException e )
        {
            LOG.error( "Error in sendMessage", e );
            throw new MessageException( e );
        }
    }


    @Override
    public MessageStatus getMessageStatus( final UUID messageId )
    {
        return null;
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
                    notificationExecutor
                            .execute( new MessageNotifier( listener, message, envelope.getSourcePeerId() ) );
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
