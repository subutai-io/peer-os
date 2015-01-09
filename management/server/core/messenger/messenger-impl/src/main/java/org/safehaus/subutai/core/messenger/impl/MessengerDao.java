package org.safehaus.subutai.core.messenger.impl;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.core.messenger.impl.dao.MessageDataService;
import org.safehaus.subutai.core.messenger.impl.model.MessageEntity;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;


/**
 * Messenger DAO
 */
public class MessengerDao
{
    private static final int WIDENING_INTERVAL_SEC = 5;
    private static final int MESSAGE_LIMIT_PER_PEER = 10;

    protected MessageDataService messageDataService;


    public MessengerDao( EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory, "EntityManagerFactory is null" );

        this.messageDataService = new MessageDataService( entityManagerFactory );
    }


    public void purgeExpiredMessages()
    {
        messageDataService.purgeMessages();
    }


    public Set<Envelope> getEnvelopes()
    {

        Set<Envelope> result = Sets.newHashSet();

        List<String> targetPeers = messageDataService.getTargetPeers();

        for ( final String targetPeer : targetPeers )
        {
            List<MessageEntity> messages =
                    messageDataService.getMessages( targetPeer, WIDENING_INTERVAL_SEC, MESSAGE_LIMIT_PER_PEER );

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
        messageDataService.markAsSent( envelope.getMessage().getId().toString() );
    }


    public void incrementDeliveryAttempts( Envelope envelope )
    {
        messageDataService.incrementDeliveryAttempts( envelope.getMessage().getId().toString() );
    }


    public void saveEnvelope( Envelope envelope )
    {
        MessageEntity messageEntity = new MessageEntity( envelope );
        messageDataService.persist( messageEntity );
    }


    public Envelope getEnvelope( UUID messageId )
    {
        MessageEntity messageEntity = messageDataService.find( messageId.toString() );
        return new Envelope( messageEntity );
    }
}
