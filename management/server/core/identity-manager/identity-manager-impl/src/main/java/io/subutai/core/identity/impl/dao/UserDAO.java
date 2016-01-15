package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.User;
import io.subutai.core.identity.impl.model.UserEntity;


/**
 * Implementation of User Dao Manager
 */
class UserDAO
{
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    public UserDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public User find( final long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        User result = null;
        try
        {
            daoManager.startTransaction( em );
            result = em.find( UserEntity.class, id );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public List<User> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<User> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from UserEntity h WHERE h.isApproved = true", User.class ).getResultList();
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public List<User> getAllSystemUsers()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<User> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from UserEntity h", User.class ).getResultList();
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    public void persist( final User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );
            em.flush();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void remove( final Long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            UserEntity item = em.find( UserEntity.class, id );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void update( final User item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.merge( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public User findByUsername( final String userName )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        User result = null;
        try
        {
            TypedQuery<UserEntity> query =
                    em.createQuery( "select u from UserEntity u where u.userName = :userName", UserEntity.class );
            query.setParameter( "userName", userName );

            List<UserEntity> users = query.getResultList();
            if ( users.size() > 0 )
            {
                result = users.iterator().next();
            }
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }
}
