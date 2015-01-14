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


    public void init()
    {
        //Init
    }
    public DaoManager()
    {
        //Destroy
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
    }


    public EntityManagerFactory getEntityManagerFactory()
    {
        return entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory entityManagerFactory )
    {
        this.entityManagerFactory = entityManagerFactory;

        if(entityManagerFactory!=null)
        {
            this.entityManager = entityManagerFactory.createEntityManager();
        }
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
    public synchronized short mergeExt(EntityManager em,Object obj)
    {
        try
        {
            if(em!=null)
            {
                em.merge( obj );
            }
            else
            {
                return  0;
            }
        }
        catch(Exception Ex)
        {
            return 0;
        }

        return 1;
    }
    public synchronized short persistExt(EntityManager em,Object obj)
    {
        try
        {
            if(em!=null)
            {
                em.persist( obj );
            }
            else
            {
                return  0;
            }
        }
        catch(Exception Ex)
        {
            return 0;
        }

        return 1;
    }
    public synchronized short removeExt(EntityManager em,Object obj)
    {
        try
        {
            if(em!=null)
            {
                em.remove( obj );
            }
            else
            {
                return  0;
            }
        }
        catch(Exception Ex)
        {
            return 0;
        }

        return 1;
    }
}
