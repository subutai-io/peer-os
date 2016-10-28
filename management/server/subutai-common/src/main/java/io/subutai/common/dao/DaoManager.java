package io.subutai.common.dao;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * DAO Manager. Manages, controls entityManagerFactory instances
 */

public class DaoManager
{
    protected static final Logger LOG = LoggerFactory.getLogger( DaoManager.class );

    private EntityManager entityManager;
    private EntityManagerFactory entityManagerFactory;
    private boolean autoCommit = true;


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
        if ( entityManagerFactory != null && entityManagerFactory.isOpen() )
        {
            entityManagerFactory.close();
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

        if ( entityManagerFactory != null )
        {
            this.entityManager = entityManagerFactory.createEntityManager();
        }
    }


    /**
     * Rollback the transaction
     *
     * @param em - EntityManager
     *
     * @return short
     */
    public short rollBackTransaction( EntityManager em )
    {
        if ( em != null && em.getTransaction().isActive() )
        {
            em.getTransaction().rollback();
        }

        return 1;
    }


    /**
     * Start the transaction
     *
     * @param em - EntityManager
     *
     * @return short
     */
    public short startTransaction( EntityManager em )
    {
        if ( em != null )
        {
            em.getTransaction().begin();
        }

        return 1;
    }


    /**
     * Commit the transaction
     *
     * @param em - EntityManager
     *
     * @return short
     */
    public short commitTransaction( EntityManager em )
    {
        if ( em != null )
        {
            em.getTransaction().commit();
        }

        return 1;
    }


    /**
     * Close EntityManager object
     *
     * @param em - EntityManager
     *
     * @return short
     */
    public short closeEntityManager( EntityManager em )
    {
        if ( em != null )
        {
            em.close();
        }

        return 1;
    }


    /**
     * Merge entity data changes (synchronized)
     *
     * @param em - EntityManager
     * @param obj - Object
     *
     * @return short
     */
    public synchronized short mergeExt( EntityManager em, Object obj )
    {
        try
        {
            if ( em != null )
            {
                em.merge( obj );
            }
            else
            {
                return 0;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            return 0;
        }

        return 1;
    }


    /**
     * Save entity data (synchronized)
     *
     * @param em - EntityManager
     * @param obj - Object
     *
     * @return short
     */
    public synchronized short persistExt( EntityManager em, Object obj )
    {
        try
        {
            if ( em != null )
            {
                em.persist( obj );
            }
            else
            {
                return 0;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            return 0;
        }

        return 1;
    }


    /**
     * Remove object (synchronized)
     *
     * @param em - EntityManager
     * @param obj - Object
     *
     * @return short
     */
    public synchronized short removeExt( EntityManager em, Object obj )
    {
        try
        {
            if ( em != null )
            {
                em.remove( obj );
            }
            else
            {
                return 0;
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );

            return 0;
        }

        return 1;
    }


    public boolean isAutoCommit()
    {
        return autoCommit;
    }


    public void setAutoCommit( final boolean autoCommit )
    {
        this.autoCommit = autoCommit;
    }
}
