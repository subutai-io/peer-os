package org.safehaus.subutai.core.messenger.impl.dao;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.messenger.impl.entity.MessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class MessageDataService implements DataService<String, MessageEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( MessageDataService.class );
    EntityManager em;


    public MessageDataService( EntityManager entityManager )
    {
        Preconditions.checkNotNull( entityManager );

        this.em = entityManager;
    }


    @Override
    public MessageEntity find( final String id )
    {
        MessageEntity result = null;

        try
        {
            result = em.find( MessageEntity.class, id );
        }
        catch ( Exception e )
        {
        }
        finally
        {
        }
        return result;
    }


    @Override
    public Collection<MessageEntity> getAll()
    {
        Collection<MessageEntity> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from MessageEntity h", MessageEntity.class ).getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
        return result;
    }


    @Override
    public void persist( final MessageEntity item )
    {
        try
        {
            em.persist( item );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    @Override
    public void remove( final String id )
    {
        try
        {
            MessageEntity item = em.find( MessageEntity.class, id );
            em.remove( item );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    @Override
    public void update( final MessageEntity item )
    {
        try
        {
            em.merge( item );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    public List<String> getTargetPeers()
    {
        List<String> result = new ArrayList<>();
        try
        {
            TypedQuery<String> query =
                    em.createQuery( "select distinct e.targetPeerId from MessageEntity e", String.class );
            result = query.getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }

        return result;
    }


    public List<MessageEntity> getMessages( final String targetPeer, final int wideningIntervalSec,
                                            final int messageLimitPerPeer )
    {

        List<MessageEntity> result = new ArrayList<>();
        try
        {
            long ts = System.currentTimeMillis();
            TypedQuery<MessageEntity> query = em.createQuery(
                    "select e from MessageEntity e where e.targetPeerId = :targetPeerId"
                            + " and e.isSent =false and e.createDate + e.attempts * 1000 * :intval < :ts1"
                            + " and :ts1 - e.createDate <= e.timeToLive * 1000 order by e.createDate asc",
                    MessageEntity.class ).setMaxResults( messageLimitPerPeer )
                                                .setParameter( "targetPeerId", targetPeer )
                                                .setParameter( "intval", wideningIntervalSec )
                                                .setParameter( "ts1", ts );
            result = query.getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }

        return result;
    }


    public void purgeMessages()
    {
        try
        {
            long ts = System.currentTimeMillis();
            Query query = em.createQuery( "delete from MessageEntity e where e.createDate + 3600 * 1000 * 24 < :ts1" )
                            .setParameter( "ts1", ts );

            query.executeUpdate();
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    public void markAsSent( String messageId )
    {
        try
        {
            Query query = em.createQuery( "update MessageEntity e set e.isSent = true where e.id = :id" )
                            .setParameter( "id", messageId );

            query.executeUpdate();
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    public void incrementDeliveryAttempts( final String messageId )
    {
        try
        {
            Query query = em.createQuery( "update MessageEntity e set e.attempts = e.attempts + 1  where e.id = :id" )
                            .setParameter( "id", messageId );

            query.executeUpdate();
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }
}
