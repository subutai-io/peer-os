package org.safehaus.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.impl.entity.CliCommandEntity;
import org.safehaus.subutai.core.identity.impl.entity.CliCommandPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * Created by talas on 2/5/15.
 */
public class CliCommandDataService implements DataService<CliCommandPK, CliCommandEntity>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( CliCommandDataService.class );
    private EntityManagerFactory emf;


    public CliCommandDataService( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf, "Please provide valid entity manager factory for CliCommands data service" );
        this.emf = emf;
    }


    @Override
    public List<CliCommandEntity> getAll()
    {
        List<CliCommandEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM CliCommandEntity p ORDER BY p.scope, p.name",
                    CliCommandEntity.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all cli commands", e );
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
    public CliCommandEntity find( final CliCommandPK id )
    {
        CliCommandEntity result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<CliCommandEntity> query =
                    em.createQuery( "SELECT p FROM CliCommandEntity p WHERE p.name = :name AND p.scope = " + ":scope",
                            CliCommandEntity.class );
            query.setParameter( "name", id.getName() );
            query.setParameter( "scope", id.getScope() );

            List<CliCommandEntity> items = query.getResultList();
            if ( items.size() > 0 )
            {
                result = items.iterator().next();
            }
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error looking for CliCommands", e );
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
    public void persist( final CliCommandEntity item )
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
            LOGGER.error( "Error while persisting cli command.", e );
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
    public void remove( final CliCommandPK id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Query query =
                    em.createQuery( "DELETE FROM CliCommandEntity p WHERE p.name = :name AND p.scope = " + ":scope" );
            query.setParameter( "name", id.getName() );
            query.setParameter( "scope", id.getScope() );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while removing cli command.", e );
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
    public void update( final CliCommandEntity item )
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
            LOGGER.error( "Error while merging cli command.", e );
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
