package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.identity.impl.model.UserTokenEntity;


/**
 *
 */
public class UserTokenDAO
{
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    public UserTokenDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    public UserToken find( String token )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        UserToken result = null;
        try
        {
            daoManager.startTransaction( em );
            result = em.find( UserTokenEntity.class, token );
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
    public List<UserToken> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<UserToken> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from UserTokenEntity h", UserToken.class ).getResultList();
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
    public void persist( final UserToken item )
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
    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            UserTokenEntity item = em.find( UserTokenEntity.class, id );
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
    public void update( final UserToken item )
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


}
