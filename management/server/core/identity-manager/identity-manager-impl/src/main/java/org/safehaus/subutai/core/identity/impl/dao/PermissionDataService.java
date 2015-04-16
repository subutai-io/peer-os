package org.safehaus.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.api.PermissionGroup;
import org.safehaus.subutai.core.identity.impl.entity.PermissionEntity;
import org.safehaus.subutai.core.identity.impl.entity.PermissionPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;


public class PermissionDataService implements DataService<PermissionPK, PermissionEntity>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionDataService.class );
    private EntityManagerFactory emf;


    public PermissionDataService( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf, "Please provide valid entity manager factory for Permissions data service" );
        this.emf = emf;
    }


    @Override
    public List<PermissionEntity> getAll()
    {
        List<PermissionEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM PermissionEntity p", PermissionEntity.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all permissions", e );
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


    public List<PermissionEntity> getAllByPermissionGroup( PermissionGroup permissionGroup )
    {
        List<PermissionEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<PermissionEntity> query =
                    em.createQuery( "SELECT p FROM PermissionEntity p WHERE p.permissionGroup = :permissionGroup",
                            PermissionEntity.class );
            query.setParameter( "permissionGroup", permissionGroup );
            result = query.getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error retrieving all permissions", e );
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
    public PermissionEntity find( final PermissionPK id )
    {
        PermissionEntity result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<PermissionEntity> query = em.createQuery(
                    "SELECT p FROM PermissionEntity p WHERE p.name = :name AND p.permissionGroup = "
                            + ":permissionGroup", PermissionEntity.class );
            query.setParameter( "name", id.getPermissionKey() );
            query.setParameter( "permissionGroup", id.getPermissionGroup() );

            List<PermissionEntity> permissions = query.getResultList();
            if ( permissions.size() > 0 )
            {
                result = permissions.iterator().next();
            }
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error looking for Permission", e );
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
    public void persist( final PermissionEntity item )
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
            LOGGER.error( "Error while persisting permission.", e );
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
    public void remove( final PermissionPK id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            PermissionEntity permission = find( id );
            em.getTransaction().begin();

            Query query = em.createQuery( "DELETE FROM PermissionEntity p WHERE p.name = :name AND p.permissionGroup = "
                            + ":permissionGroup" );
            query.setParameter( "name", id.getPermissionKey() );
            query.setParameter( "permissionGroup", id.getPermissionGroup() );
            query.executeUpdate();

            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( "Error while removing permission.", e );
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
    public void update( final PermissionEntity item )
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
            LOGGER.error( "Error while merging permission.", e );
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
