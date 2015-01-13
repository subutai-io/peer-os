package org.safehaus.subutai.common.dao;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * Created by nisakov on 1/8/15.
 */

public class DaoManager
{
    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private short injectionMethod = 0;


    public short getInjectionMethod()
    {
        return injectionMethod;
    }


    public void setInjectionMethod( final short injectionMethod )
    {
        this.injectionMethod = injectionMethod;
    }


    public DaoManager()
    {
        //Destroy
    }

    public void init()
    {
        //Init
    }
    public void destroy()
    {
        if(entityManagerFactory!=null)
        {
            if(entityManagerFactory.isOpen())
            {
                entityManagerFactory.close();
            }
        }
    }

    public EntityManager getEntityManager()
    {
        return entityManager;
    }
    public EntityManager getEntityManagerFromFactory()
    {
        return entityManagerFactory.createEntityManager();
    }

    public void setEntityManager( final EntityManager entityManager )
    {
        this.entityManager = entityManager;
        injectionMethod = 2;
    }


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;
        injectionMethod = 1;
    }
    public short rollBackTransaction(EntityManager em)
    {
        if(em!=null)
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        return 1;
    }
    public short startTransaction(EntityManager em)
    {
        if(em!=null)
        {
            em.getTransaction().begin();
        }

        return 1;
    }
    public short commitTransaction(EntityManager em)
    {
        if(em!=null)
        {
            em.getTransaction().commit();
        }

        return 1;
    }
    public short closeEntityManager(EntityManager em)
    {
        if(em!=null)
        {
            em.close();
        }

        return 1;
    }
}
