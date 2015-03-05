package org.safehaus.subutai.core.channel.impl.token;


import java.util.List;

import org.safehaus.subutai.core.channel.api.entity.IUserChannelToken;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;
import org.safehaus.subutai.core.channel.impl.entity.UserChannelToken;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;



/**
 * Created by nisakov on 3/3/15.
 */

public class ChannelTokenManagerImpl implements ChannelTokenManager
{
    private EntityManagerFactory EntityManagerFactory = null;



    public EntityManagerFactory getEntityManagerFactory()
    {
        return EntityManagerFactory;
    }

    public void setEntityManagerFactory( EntityManagerFactory entityManagerFactory )
    {
        EntityManagerFactory = entityManagerFactory;
    }


    /***********************************************************************************************************
     *
     * */
    public long getUserChannelToken(String token)
    {
        long user_id = 0;
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select ucht FROM UserChannelToken AS ucht WHERE ucht.token=:tokenParam and ucht.validPeriod>0" );
            query.setParameter( "tokenParam", token );
            UserChannelToken userChannelToken = (UserChannelToken)query.getSingleResult();

            if(userChannelToken!=null)
            {
                user_id = userChannelToken.getUserId();
            }
            else
            {
                user_id = 0;
            }
        }
        catch ( Exception e )
        {
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }

        return  user_id;

    }

    /***********************************************************************************************************
     *
     * */
    public void setTokenValidity()
    {
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();

            Query query;
            query = entityManager.createNativeQuery(" update user_channel_token set valid_period  = "
                    + " case when (valid_period - datediff(hour,user_channel_token.date,CURRENT_TIMESTAMP))<=0 then  0"
                    + " else  valid_period - datediff(hour,user_channel_token.date,CURRENT_TIMESTAMP)"
                    + " end" );

            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if(entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }
    }
    /***********************************************************************************************************
     *
     * */
    public void saveUserChannelToken(IUserChannelToken userChannelToken)
    {
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            entityManager.getTransaction().begin();
            entityManager.merge(userChannelToken);
            entityManager.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if(entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }
    }
    /***********************************************************************************************************
     *
     * */
    public void removeUserChannelToken(String token)
    {
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

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
            if(entityManager.getTransaction().isActive())
                entityManager.getTransaction().rollback();
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }
    }
    /***********************************************************************************************************
     *
     * */
    public List<IUserChannelToken> getUserChannelTokenData(long userId)
    {
        List userChannelTokenList = null;
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select ucht FROM UserChannelToken AS ucht WHERE ucht.userId = :user_id" );
            query.setParameter( "user_id", userId );
            userChannelTokenList = query.getResultList();
        }
        catch ( Exception e )
        {
            System.out.println(e.toString());
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }

        return  userChannelTokenList;

    }
    /***********************************************************************************************************
     *
     * */
    public List<IUserChannelToken> getAllUserChannelTokenData()
    {
        List userChannelTokenList = null;
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select u FROM UserChannelToken AS u" );
            userChannelTokenList = query.getResultList();
        }
        catch ( Exception e )
        {
            System.out.println(e.toString());
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }

        return  userChannelTokenList;

    }

    /***********************************************************************************************************
     *
     * */
    public IUserChannelToken createUserChannelToken()
    {
        return new UserChannelToken();
    }

}