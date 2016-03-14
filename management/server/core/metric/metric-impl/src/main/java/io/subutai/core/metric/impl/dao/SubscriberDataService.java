package io.subutai.core.metric.impl.dao;


import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import io.subutai.common.exception.DaoException;
import io.subutai.core.metric.impl.model.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


public class SubscriberDataService
{
    private EntityManagerFactory emf;
    private Logger LOGGER = LoggerFactory.getLogger( SubscriberDataService.class );


    public SubscriberDataService( final EntityManagerFactory emf ) throws DaoException
    {
        Preconditions.checkNotNull( emf, "EntityManagerFactory cannot be null." );
        this.emf = emf;
        try
        {
            this.emf.createEntityManager().close();
        }
        catch ( PersistenceException e )
        {
            LOGGER.error( "Couldn't initialize EntityManager in SubscriberDataService." );
            throw new DaoException( e );
        }
    }


    public void setEntityManagerFactory( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf, " EntityManagerFactory cannot be null." );
        this.emf = emf;
    }


    public void update( final String environmentId, final String subscriberId ) throws DaoException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            Subscriber subscriber = new Subscriber( environmentId, subscriberId );
            em.merge( subscriber );

            em.getTransaction().commit();
        }
        catch ( PersistenceException e )
        {
            LOGGER.error( "Instance is not an entity or command invoked on a container-managed entity manager." );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            throw new DaoException( e );
        }
        finally
        {
            em.close();
        }
    }


    public void remove( final String environmentId, final String subscriberId ) throws DaoException
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            Query query = em.createQuery(
                    "DELETE FROM Subscriber s WHERE s.environmentId = :environmentId AND s.subscriberId = "
                            + ":subscriberId" );
            query.setParameter( "environmentId", environmentId );
            query.setParameter( "subscriberId", subscriberId );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( PersistenceException e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            LOGGER.error( "Query string found to be invalid." );
            throw new DaoException( e );
        }
        finally
        {
            em.close();
        }
    }


    public Set<String> findHandlersByEnvironment( final String environmentId ) throws DaoException
    {
        Set<String> result = new HashSet<>();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();

            TypedQuery<String> query =
                    em.createQuery( "select s.subscriberId from Subscriber s where s.environmentId = :environmentId",
                            String.class );
            query.setParameter( "environmentId", environmentId );
            result.addAll( query.getResultList() );

            em.getTransaction().commit();
        }
        catch ( PersistenceException e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
            LOGGER.error( "Error getting subscriberIds" );
            throw new DaoException( e );
        }
        finally
        {
            em.close();
        }
        return result;
    }
}
