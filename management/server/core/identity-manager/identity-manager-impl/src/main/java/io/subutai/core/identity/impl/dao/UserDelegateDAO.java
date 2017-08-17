package io.subutai.core.identity.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.UserDelegate;
import io.subutai.core.identity.impl.model.UserDelegateEntity;


/**
 *
 */
class UserDelegateDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( UserDelegateDAO.class );

    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    UserDelegateDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    UserDelegate find( String id )
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

            LOG.error( e.getMessage() );
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
    List<UserDelegate> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<UserDelegate> result = Lists.newArrayList();
        try
        {
            TypedQuery<UserDelegateEntity> query =
                    em.createQuery( "select h from UserDelegateEntity h", UserDelegateEntity.class );
            result.addAll( query.getResultList() );
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
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
    void persist( final UserDelegate item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    void remove( final String id )
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

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public void update( final UserDelegate item )
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

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    UserDelegate findByUserId( final long userId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UserDelegate tk = null;
        try
        {
            List<UserDelegateEntity> result;
            TypedQuery<UserDelegateEntity> qr =
                    em.createQuery( "select h from UserDelegateEntity h where h.userId=:userId",
                            UserDelegateEntity.class );
            qr.setParameter( "userId", userId );
            result = qr.getResultList();

            if ( result != null && !result.isEmpty() )
            {
                tk = result.get( 0 );
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return tk;
    }
}
