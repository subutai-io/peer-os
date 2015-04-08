package org.safehaus.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.impl.entity.RestEndpointScopeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class RestEndpointDataService implements DataService<String, RestEndpointScopeEntity>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RestEndpointDataService.class );
    private EntityManagerFactory emf;


    public RestEndpointDataService( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf,
                "Please provide valid entity manager factory for RestEndpointScopeEntity data service" );
        this.emf = emf;
    }


    @Override
    public List<RestEndpointScopeEntity> getAll()
    {
        List<RestEndpointScopeEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM RestEndpointScopeEntity p", RestEndpointScopeEntity.class )
                       .getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all RestEndpointScopeEntity entity", e );
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
    public RestEndpointScopeEntity find( final String id )
    {
        RestEndpointScopeEntity result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<RestEndpointScopeEntity> query =
                    em.createQuery( "SELECT p FROM RestEndpointScopeEntity p WHERE p.restEndpoint = :restEndpoint",
                            RestEndpointScopeEntity.class );
            query.setParameter( "restEndpoint", id );

            List<RestEndpointScopeEntity> resultList = query.getResultList();
            if ( resultList.size() > 0 )
            {
                result = resultList.iterator().next();
            }
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error looking for RestEndpointScopeEntity entity", e );
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
    public void persist( final RestEndpointScopeEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
            em.getTransaction().commit();
            em.detach( item );
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while persisting RestEndpointScopeEntity entity.", e );
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

            Query query =
                    em.createQuery( "DELETE FROM RestEndpointScopeEntity p WHERE p.restEndpoint = :restEndpoint" );
            query.setParameter( "restEndpoint", id );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while removing RestEndpointScopeEntity entity.", e );
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
    public void update( final RestEndpointScopeEntity item )
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
            LOGGER.error( "Error while merging RestEndpointScopeEntity entity.", e );
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


    public EntityManagerFactory getEmf()
    {
        return emf;
    }


    public void setEmf( final EntityManagerFactory emf )
    {
        this.emf = emf;
    }
}
