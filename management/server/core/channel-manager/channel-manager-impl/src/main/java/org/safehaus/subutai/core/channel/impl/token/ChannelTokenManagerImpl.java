package org.safehaus.subutai.core.channel.impl.token;


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
            query = entityManager.createQuery( "select ucht FROM UserChannelToken AS ucht WHERE ucht.token=:tokenParam and ucht.status=1" );
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
            query = entityManager.createQuery( "update UserChannelToken AS ucht set ucht.status = 0 where DATEDIFF(hour,ucht.date ,CURRENT_TIMESTAMP)> ucht.valid_period " );

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
    public IUserChannelToken getUserChannelTokenData(long userId)
    {
        UserChannelToken userChannelToken = null;

        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select u FROM UserChannelToken AS u WHERE u.userId = :user_id" );
            query.setParameter( "user_id", userId );
            userChannelToken = (UserChannelToken)query.getSingleResult();
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

        return  userChannelToken;

    }

    public IUserChannelToken createUserChannelToken()
    {
        return new UserChannelToken();
    }

}
