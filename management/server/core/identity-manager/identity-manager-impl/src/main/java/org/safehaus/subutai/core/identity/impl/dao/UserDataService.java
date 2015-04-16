package org.safehaus.subutai.core.identity.impl.dao;


import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.identity.api.User;
import org.safehaus.subutai.core.identity.impl.entity.UserEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class UserDataService implements DataService<Long, User>
{
    private static final Logger LOG = LoggerFactory.getLogger( UserDataService.class );
    DaoManager daoManager;


    public UserDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    //    public void setEntityManagerFactory( final EntityManagerFactory emf )
    //    {
    //        this.emf = emf;
    //    }


    @Override
    public UserEntity find( final Long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        UserEntity result = null;
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
    public Collection<User> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        Collection<User> result = Lists.newArrayList();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from UserEntity h", User.class ).getResultList();
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
    public void persist( final User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
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
        EntityManager em = daoManager.getEntityManagerFromFactory();
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
    public void update( final User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
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
        EntityManager em = daoManager.getEntityManagerFromFactory();
        User result = null;
        try
        {
            em.getTransaction().begin();
            TypedQuery<UserEntity> query =
                    em.createQuery( "select u from UserEntity u where u.username = :username", UserEntity.class );
            query.setParameter( "username", username );

            List<UserEntity> users = query.getResultList();
            if ( users.size() > 0 )
            {
                result = users.iterator().next();
            }
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
}
