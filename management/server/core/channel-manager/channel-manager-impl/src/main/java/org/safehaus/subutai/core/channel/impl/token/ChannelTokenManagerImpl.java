package org.safehaus.subutai.core.channel.impl.token;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;
import org.safehaus.subutai.core.channel.impl.entity.UserChannelToken;


/**
 * Class manages User tokens.
 */

public class ChannelTokenManagerImpl implements ChannelTokenManager
{
    private EntityManagerFactory entityManagerFactory = null;


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }


    /**
     * ********************************************************************************************************
     */
    public long getUserChannelTokenId( String token )
    {
        long userId = 0;
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery(
                    "select ucht FROM UserChannelToken AS ucht WHERE ucht.token=:tokenParam and ucht.validPeriod>0" );
            query.setParameter( "tokenParam", token );
            UserChannelToken userChannelToken = ( UserChannelToken ) query.getSingleResult();

            if ( userChannelToken != null )
            {
                userId = userChannelToken.getUserId();
            }
            else
            {
                userId = 0;
            }
        }
        catch ( Exception ignore )
        {
            //ignore
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }

        return userId;
    }


    /**
     * ********************************************************************************************************
     */
    public IUserChannelToken getUserChannelToken( String token )
    {
        IUserChannelToken userChannelToken = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery(
                    "select ucht FROM UserChannelToken AS ucht WHERE ucht.token=:tokenParam and ucht.validPeriod>0" );
            query.setParameter( "tokenParam", token );
            userChannelToken = ( UserChannelToken ) query.getSingleResult();
        }
        catch ( Exception ignore )
        {
            //ignore
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }

        return userChannelToken;
    }


    /**
     * ********************************************************************************************************
     */
    public void setTokenValidity()
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            //-------- Update Validity Period
            // ------------------------------------------------------------------------------
            Query query;

            query = entityManager.createNativeQuery(
                    " UPDATE user_channel_token SET valid_period  = " + " CASE WHEN (valid_period-1)<0 THEN  0"
                            + " ELSE valid_period-1" + " END" );

            query.executeUpdate();
            //--------------------------------------------------------------------------------------

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }
    }


    /**
     * ********************************************************************************************************
     */
    public void saveUserChannelToken( IUserChannelToken userChannelToken )
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();
            entityManager.merge( userChannelToken );
            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }
    }


    /**
     * ********************************************************************************************************
     */
    public void removeUserChannelToken( String token )
    {
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            Query query;
            query = entityManager.createQuery( "delete from UserChannelToken AS ucht where ucht.token=:tokenParam " );
            query.setParameter( "tokenParam", token );
            query.executeUpdate();

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( entityManager.getTransaction().isActive() )
            {
                entityManager.getTransaction().rollback();
            }
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }
    }


    /**
     * ********************************************************************************************************
     */
    public List<IUserChannelToken> getUserChannelTokenData( long userId )
    {
        List userChannelTokenList = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager
                    .createQuery( "select ucht FROM UserChannelToken AS ucht WHERE ucht.userId = :user_id" );
            query.setParameter( "user_id", userId );
            userChannelTokenList = query.getResultList();
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }

        return userChannelTokenList;
    }


    /**
     * ********************************************************************************************************
     */
    public List<IUserChannelToken> getAllUserChannelTokenData()
    {
        List userChannelTokenList = null;
        EntityManager entityManager = entityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select u FROM UserChannelToken AS u order by u.userId" );
            userChannelTokenList = query.getResultList();
        }
        catch ( Exception e )
        {
            System.out.println( e.toString() );
        }
        finally
        {
            if ( entityManager.isOpen() )
            {
                entityManager.close();
            }
        }

        return userChannelTokenList;
    }


    /**
     * ********************************************************************************************************
     */
    public IUserChannelToken createUserChannelToken()
    {
        return new UserChannelToken();
    }
}