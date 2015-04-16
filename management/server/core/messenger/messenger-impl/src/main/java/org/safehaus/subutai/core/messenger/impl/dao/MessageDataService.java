package org.safehaus.subutai.core.messenger.impl.dao;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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
    EntityManagerFactory emf;


    public MessageDataService( EntityManagerFactory entityManagerFactory )
    {
        Preconditions.checkNotNull( entityManagerFactory );

        this.emf = entityManagerFactory;
    }


    @Override
    public MessageEntity find( final String id )
    {
        MessageEntity result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( MessageEntity.class, id );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
        return result;
    }


    @Override
    public Collection<MessageEntity> getAll()
    {
        Collection<MessageEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from MessageEntity h", MessageEntity.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
        return result;
    }


    @Override
    public void persist( final MessageEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    @Override
    public void remove( final String id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            MessageEntity item = em.find( MessageEntity.class, id );
            em.remove( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    @Override
    public void update( final MessageEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.merge( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    public List<String> getTargetPeers()
    {
        List<String> result = new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            TypedQuery<String> query =
                    em.createQuery( "select distinct e.targetPeerId from MessageEntity e", String.class );
            result = query.getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }

        return result;
    }


    public List<MessageEntity> getMessages( final String targetPeer, final int wideningIntervalSec,
                                            final int messageLimitPerPeer )
    {

        List<MessageEntity> result = new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
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
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }

        return result;
    }


    public void purgeMessages()
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            long ts = System.currentTimeMillis();
            Query query = em.createQuery( "delete from MessageEntity e where e.createDate + 3600 * 1000 * 24 < :ts1" )
                            .setParameter( "ts1", ts );

            query.executeUpdate();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    public void markAsSent( String messageId )
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Query query = em.createQuery( "update MessageEntity e set e.isSent = true where e.id = :id" )
                            .setParameter( "id", messageId );

            query.executeUpdate();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    public void incrementDeliveryAttempts( final String messageId )
    {

        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Query query = em.createQuery( "update MessageEntity e set e.attempts = e.attempts + 1  where e.id = :id" )
                            .setParameter( "id", messageId );

            query.executeUpdate();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }
}
