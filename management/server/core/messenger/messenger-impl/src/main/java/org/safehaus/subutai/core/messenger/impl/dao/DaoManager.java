package org.safehaus.subutai.core.messenger.impl.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static java.lang.Thread.sleep;


/**
 * Created by nisakov on 1/8/15.
 */

public class DaoManager
{
    private  EntityManager entityManager;
    private  EntityManagerFactory entityManagerFactory;


    public EntityManager getEntityManager()
    {
        return entityManagerFactory.createEntityManager();
    }


    public void setEntityManager( final EntityManager entityManager )
    {
        this.entityManager = entityManager;
    }


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
    }
}
