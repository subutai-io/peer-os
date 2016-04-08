package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.impl.model.UserDelegateEntity;


/**
 *
 */
public class UserDelegateDAO
{
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    public UserDelegateDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public UserDelegate find( String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        UserDelegate result = null;
        try
        {
            daoManager.startTransaction( em );
            result = em.find( UserDelegateEntity.class, id );
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
    public List<UserDelegate> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<UserDelegate> result = Lists.newArrayList();
        Query query = null;
        try
        {
            query = em.createQuery( "select h from UserDelegateEntity h", UserDelegateEntity.class );
            result = ( List<UserDelegate> ) query.getResultList();
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
    public void persist( final UserDelegate item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );
            //em.flush();
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
    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            UserDelegateEntity item = em.find( UserDelegateEntity.class, id );
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
    public void update( final UserDelegate item)
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
    public UserDelegate findByUserId( final long userId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UserDelegate tk = null;
        try
        {
            List<UserDelegate> result = null;
            Query qr = em.createQuery( "select h from UserDelegateEntity h where h.userId=:userId", UserDelegate.class );
            qr.setParameter( "userId", userId );
            result = qr.getResultList();

            if ( result != null )
            {
                tk = result.get( 0 );
            }
        }
        catch ( Exception e )
        {
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return tk;
    }


}
