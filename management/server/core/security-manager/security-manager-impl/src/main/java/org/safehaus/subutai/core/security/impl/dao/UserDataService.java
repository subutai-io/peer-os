package org.safehaus.subutai.core.security.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.security.api.User;
import org.safehaus.subutai.core.security.impl.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class UserDataService implements DataService<Long, UserEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( UserDataService.class );
    EntityManagerFactory emf;


    public UserDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory emf )
    {
        this.emf = emf;
    }


    @Override
    public UserEntity find( final Long id )
    {
        UserEntity result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( UserEntity.class, id );
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
    public Collection<UserEntity> getAll()
    {
        Collection<UserEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from UserEntity h", UserEntity.class ).getResultList();
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
    public void persist( final UserEntity item )
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
    public void remove( final Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            UserEntity item = em.find( UserEntity.class, id );
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
    public void update( final UserEntity item )
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


    public User findByUsername( final String username )
    {
        UserEntity result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Query query = em.createQuery( "select u from UserEntity u where u.username = :username" );

            result = ( UserEntity ) query.getSingleResult();
            em.getTransaction().commit();
        }
        catch ( NoResultException ignore )
        {
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
}
