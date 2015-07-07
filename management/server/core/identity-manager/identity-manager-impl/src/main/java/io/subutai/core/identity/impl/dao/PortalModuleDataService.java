package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import io.subutai.core.identity.impl.entity.PortalModuleScopeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class PortalModuleDataService implements DataService<String, PortalModuleScopeEntity>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PortalModuleDataService.class );
    private EntityManagerFactory emf;


    public PortalModuleDataService( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf,
                "Please provide valid entity manager factory for PortalModuleScopeEntity data service" );
        this.emf = emf;
    }


    @Override
    public List<PortalModuleScopeEntity> getAll()
    {
        List<PortalModuleScopeEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM PortalModuleScopeEntity p ORDER BY p.moduleKey, p.moduleName",
                    PortalModuleScopeEntity.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all PortalModuleScopeEntity entity", e );
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
    public PortalModuleScopeEntity find( final String id )
    {
        PortalModuleScopeEntity result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<PortalModuleScopeEntity> query =
                    em.createQuery( "SELECT p FROM PortalModuleScopeEntity p WHERE p.moduleKey = :moduleKey",
                            PortalModuleScopeEntity.class );
            query.setParameter( "moduleKey", id );

            List<PortalModuleScopeEntity> permissions = query.getResultList();
            if ( permissions.size() > 0 )
            {
                result = permissions.iterator().next();
            }
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error looking for PortalModuleScopeEntity entity", e );
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
    public void persist( final PortalModuleScopeEntity item )
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
            LOGGER.error( "Error while persisting PortalModuleScopeEntity entity.", e );
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

            Query query = em.createQuery( "DELETE FROM PortalModuleScopeEntity p WHERE p.moduleKey = :moduleKey" );
            query.setParameter( "moduleKey", id );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while removing PortalModuleScopeEntity entity.", e );
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
    public void update( final PortalModuleScopeEntity item )
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
            LOGGER.error( "Error while merging PortalModuleScopeEntity entity.", e );
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
