package org.safehaus.subutai.core.metric.impl.dao;


import java.util.HashSet;
import java.util.Set;


import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.core.metric.impl.model.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Created by talas on 12/12/14.
 */
public class SubscriberDataService
{
    private EntityManager em;
    private Logger LOGGER = LoggerFactory.getLogger( SubscriberDataService.class );


    public SubscriberDataService( final EntityManager em ) throws DaoException
    {
        Preconditions.checkNotNull( em, "EntityManager cannot be null." );
        this.em = em;
       
    }


    public void setEmf( final EntityManager em )
    {
        Preconditions.checkNotNull( em, "EntityManager cannot be null." );
        this.em = em;
    }


    public void update( final String environmentId, final String subscriberId ) throws DaoException
    {
        try
        {

            Subscriber subscriber = new Subscriber( environmentId, subscriberId );
            em.merge( subscriber );
            em.flush();
        }
        catch ( PersistenceException e )
        {
            LOGGER.error( "Instance is not an entity or command invoked on a container-managed entity manager." );
            throw new DaoException( e );
        }
        finally
        {
        }
    }


    public void remove( final String environmentId, final String subscriberId ) throws DaoException
    {
        try
        {

            Query query = em.createQuery(
                    "DELETE FROM Subscriber s WHERE s.environmentId = :environmentId AND s.subscriberId = "
                            + ":subscriberId" );
            query.setParameter( "environmentId", environmentId );
            query.setParameter( "subscriberId", subscriberId );
            query.executeUpdate();

            em.flush();
        }
        catch ( PersistenceException e )
        {
            LOGGER.error( "Query string found to be invalid." );
            throw new DaoException( e );
        }
        finally
        {
        }
    }


    public Set<String> getEnvironmentSubscriberIds( final String environmentId ) throws DaoException
    {
        Set<String> result = new HashSet<>();
        try
        {
            TypedQuery<String> query =
                    em.createQuery( "select s.subscriberId from Subscriber s where s.environmentId = :environmentId",
                            String.class );
            query.setParameter( "environmentId", environmentId );
            result.addAll( query.getResultList() );

            em.flush();
        }
        catch ( PersistenceException e )
        {
            LOGGER.error( "Error getting subscriberIds" );
            throw new DaoException( e );
        }
        finally
        {
        }
        return result;
    }
}
