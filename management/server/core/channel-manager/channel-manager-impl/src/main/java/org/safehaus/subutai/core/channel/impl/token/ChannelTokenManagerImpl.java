package org.safehaus.subutai.core.channel.impl.token;


import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.core.channel.api.token.ChannelTokenManager;
import org.safehaus.subutai.core.channel.impl.entity.UserChannelToken;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.sql.Timestamp;




/**
 * Created by nisakov on 3/3/15.
 */

public class ChannelTokenManagerImpl
{
    private static EntityManagerFactory EntityManagerFactory = null;


    public static EntityManagerFactory getEntityManagerFactory()
    {
        return EntityManagerFactory;
    }

    public static void setEntityManagerFactory( EntityManagerFactory entityManagerFactory )
    {
        EntityManagerFactory = entityManagerFactory;
    }


    /***********************************************************************************************************
     *
     * */
    public static long getUserChannelToken(String token)
    {
        long user_id = 0;
        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select user_id FROM UserChannelToken AS ucht WHERE and ucht.token=:token and ucht.status=1" );
            query.setParameter( "token", token );
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
    public static void setTokenValidity()
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
    public static void saveUserChannelToken(UserChannelToken userChannelToken)
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
    public static UserChannelToken getUserChannelTokenData(long userId)
    {
        UserChannelToken userChannelToken = null;

        EntityManager entityManager = EntityManagerFactory.createEntityManager();

        try
        {
            Query query;
            query = entityManager.createQuery( "select * FROM UserChannelToken AS ucht WHERE and ucht.user_id=:user_id" );
            query.setParameter( "user_id", userId );
            userChannelToken = (UserChannelToken)query.getSingleResult();
        }
        catch ( Exception e )
        {
        }
        finally
        {
            if(entityManager.isOpen())
                entityManager.close();
        }

        return  userChannelToken;

    }

}
