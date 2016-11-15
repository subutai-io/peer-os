package io.subutai.core.messenger.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import io.subutai.core.messenger.impl.dao.MessageDao;
import io.subutai.core.messenger.impl.entity.MessageEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Messenger DAO
 */
public class MessengerDataService
{
    private static final int WIDENING_INTERVAL_SEC = 5;
    private static final int MESSAGE_LIMIT_PER_PEER = 10;

    protected MessageDao messageDao;


    public MessengerDataService( EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory, "EntityManagerFactory is null" );

        this.messageDao = new MessageDao( entityManagerFactory );
    }


    public void purgeExpiredMessages()
    {
        messageDao.purgeMessages();
    }


    public Set<Envelope> getEnvelopes()
    {

        Set<Envelope> result = Sets.newHashSet();

        List<String> targetPeers = messageDao.getTargetPeers();

        for ( final String targetPeer : targetPeers )
        {
            List<MessageEntity> messages =
                    messageDao.getMessages( targetPeer, WIDENING_INTERVAL_SEC, MESSAGE_LIMIT_PER_PEER );

            Set<Envelope> envelopes = new HashSet<>();
            envelopes.addAll( buildEnvelopes( messages ) );
            result.addAll( envelopes );
        }

        return result;
    }


    protected Set<Envelope> buildEnvelopes( final List<MessageEntity> messages )
    {
        Set<Envelope> result = Sets.newHashSet();
        for ( final MessageEntity message : messages )
        {
            Envelope envelope = new Envelope( message );
            result.add( envelope );
        }
        return result;
    }


    public void markAsSent( Envelope envelope )
    {
        messageDao.markAsSent( envelope.getMessage().getId().toString() );
    }


    public void incrementDeliveryAttempts( Envelope envelope )
    {
        messageDao.incrementDeliveryAttempts( envelope.getMessage().getId().toString() );
    }


    public void saveEnvelope( Envelope envelope )
    {
        MessageEntity messageEntity = new MessageEntity( envelope );
        messageDao.persist( messageEntity );
    }


    public Envelope getEnvelope( UUID messageId )
    {
        MessageEntity messageEntity = messageDao.find( messageId.toString() );
        if ( messageEntity == null )
        {
            return null;
        }
        return new Envelope( messageEntity );
    }
}
