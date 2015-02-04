package org.safehaus.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.impl.entity.Permission;
import org.safehaus.subutai.core.identity.impl.entity.PermissionGroup;
import org.safehaus.subutai.core.identity.impl.entity.PermissionPK;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


/**
 * Created by talas on 2/5/15.
 */
public class PermissionsDataService implements DataService<PermissionPK, Permission>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( PermissionsDataService.class );
    private EntityManagerFactory emf;


    @Override
    public List<Permission> getAll()
    {
        List<Permission> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "SELECT p FROM Permission p", Permission.class ).getResultList();
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


    public List<Permission> getAllByPermissionGroup( PermissionGroup permissionGroup )
    {
        List<Permission> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<Permission> query =
                    em.createQuery( "SELECT p FROM Permission p WHERE p.permissionGroup = :permissionGroup",
                            Permission.class );
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
    public Permission find( final PermissionPK id )
    {
        Permission result = null;
        EntityManager em = emf.createEntityManager();

        try
        {
            em.getTransaction().begin();
            TypedQuery<Permission> query = em.createQuery(
                    "SELECT p FROM Permission p WHERE p.permissionKey = :permissionKey AND p.permissionGroup = "
                            + ":permissionGroup", Permission.class );
            query.setParameter( "permissionKey", id.getPermissionKey() );
            query.setParameter( "permissionGroup", id.getPermissionGroup() );

            List<Permission> permissions = query.getResultList();
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
    public void persist( final Permission item )
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
            Permission permission = find( id );
            em.getTransaction().begin();

            em.remove( permission );

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
    public void update( final Permission item )
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
