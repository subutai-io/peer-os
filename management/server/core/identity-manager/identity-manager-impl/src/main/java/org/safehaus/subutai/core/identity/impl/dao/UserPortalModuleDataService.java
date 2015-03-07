package org.safehaus.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.impl.entity.UserPortalModuleEntity;
import org.safehaus.subutai.core.identity.impl.entity.UserPortalModulePK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


/**
 * Created by talas on 2/5/15.
 */
public class UserPortalModuleDataService implements DataService<UserPortalModulePK, UserPortalModuleEntity>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( UserPortalModuleDataService.class );
    private EntityManagerFactory emf;


    public UserPortalModuleDataService( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf, "Please provide valid entity manager factory for Permissions data service" );
        this.emf = emf;
    }


    @Override
    public List<UserPortalModuleEntity> getAll()
    {
        List<UserPortalModuleEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM UserPortalModuleEntity p", UserPortalModuleEntity.class )
                       .getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all userPortalModule entity", e );
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
    public UserPortalModuleEntity find( final UserPortalModulePK id )
    {
        UserPortalModuleEntity result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<UserPortalModuleEntity> query = em.createQuery(
                    "SELECT p FROM UserPortalModuleEntity p WHERE p.moduleKey = :moduleKey AND p.moduleName = "
                            + ":moduleName", UserPortalModuleEntity.class );
            query.setParameter( "moduleKey", id.getModuleKey() );
            query.setParameter( "moduleName", id.getModuleName() );

            List<UserPortalModuleEntity> permissions = query.getResultList();
            if ( permissions.size() > 0 )
            {
                result = permissions.iterator().next();
            }
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error looking for userPortalModule entity", e );
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
    public void persist( final UserPortalModuleEntity item )
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
            LOGGER.error( "Error while persisting userPortalModule entity.", e );
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
    public void remove( final UserPortalModulePK id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            UserPortalModuleEntity permission = find( id );
            em.getTransaction().begin();

            Query query = em.createQuery(
                    "DELETE FROM UserPortalModuleEntity p WHERE p.moduleKey = :moduleKey AND p.moduleName = "
                            + ":moduleName" );
            query.setParameter( "moduleKey", id.getModuleKey() );
            query.setParameter( "moduleName", id.getModuleName() );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while removing userPortalModule entity.", e );
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
    public void update( final UserPortalModuleEntity item )
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
            LOGGER.error( "Error while merging userPortalModule entity.", e );
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
