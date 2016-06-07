package io.subutai.core.identity.impl.dao;


import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.identity.api.model.UserToken;
import io.subutai.core.identity.impl.model.UserTokenEntity;


/**
 *
 */
class UserTokenDAO
{
    private static final Logger logger = LoggerFactory.getLogger( UserTokenDAO.class );
    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    UserTokenDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    UserToken find( String token )
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
            logger.error( "**** Error in UserTokenDAO:", e );
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
    UserToken findValid( String token )
    {
        UserToken result = find( token );
        try
        {
            if ( result != null )
            {
                Date curDate = new Date( System.currentTimeMillis() );
                if ( !result.getValidDate().after( curDate ) )
                {
                    return null;
                }
            }
        }
        catch ( Exception e )
        {
            logger.error( "**** Error in UserTokenDAO:", e );
        }

        return result;
    }


    /* *************************************************
     *
     */
    void removeInvalid()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            Query query =
                    em.createQuery( "delete from UserTokenEntity ut where ut.type=1 and ut.validDate<:CurrentDate" );
            query.setParameter( "CurrentDate", new Date( System.currentTimeMillis() ) );
            query.executeUpdate();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
            logger.error( "**** Error in UserTokenDAO:", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    List<UserToken> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<UserToken> result = Lists.newArrayList();
        try
        {
            TypedQuery<UserTokenEntity> query =
                    em.createQuery( "select h from UserTokenEntity h", UserTokenEntity.class );
            result.addAll( query.getResultList() );
        }
        catch ( Exception e )
        {
            logger.error( "**** Error in UserTokenDAO:", e );
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
    void persist( UserToken item )
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
            logger.error( "**** Error in UserTokenDAO:", e );
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
            UserTokenEntity item = em.find( UserTokenEntity.class, id );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );
            logger.error( "**** Error in UserTokenDAO:", e );
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
            logger.error( "**** Error in UserTokenDAO:", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    UserToken findByUserId( final long userId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UserToken tk = null;
        try
        {
            List<UserTokenEntity> result;
            TypedQuery<UserTokenEntity> qr =
                    em.createQuery( "select h from UserTokenEntity h where h.userId=:userId", UserTokenEntity.class );
            qr.setParameter( "userId", userId );
            result = qr.getResultList();

            if ( result != null )
            {
                if ( result.size() > 0 )
                {
                    tk = result.get( 0 );
                }
            }
        }
        catch ( Exception e )
        {
            logger.error( "**** Error in UserTokenDAO:", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return tk;
    }


    /* *************************************************
     *
     */
    UserToken findByDetails( final long userId, final int tokenType )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UserToken tk = null;
        try
        {
            List<UserTokenEntity> result;
            TypedQuery<UserTokenEntity> qr =
                    em.createQuery( "select h from UserTokenEntity h where h.userId=:userId and h.type=:tokenType",
                            UserTokenEntity.class );
            qr.setParameter( "userId", userId );
            qr.setParameter( "tokenType", tokenType );

            result = qr.getResultList();

            if ( result != null )
            {
                if ( result.size() > 0 )
                {
                    tk = result.get( 0 );
                }
            }
        }
        catch ( Exception e )
        {
            logger.error( "**** Error in UserTokenDAO:", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return tk;
    }


    /* *************************************************
     *
     */
    UserToken findValidByUserId( final long userId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        UserToken tk = null;
        try
        {
            List<UserTokenEntity> result;
            TypedQuery<UserTokenEntity> qr = em.createQuery(
                    "select h from UserTokenEntity h where h.userId=:userId and h.validDate>=:validDate",
                    UserTokenEntity.class );
            qr.setParameter( "userId", userId );
            qr.setParameter( "validDate", new Date( System.currentTimeMillis() ) );
            result = qr.getResultList();

            if ( result != null )
            {
                tk = result.get( 0 );
            }
        }
        catch ( Exception e )
        {
            logger.error( "**** Error in UserTokenDAO:", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return tk;
    }
}
