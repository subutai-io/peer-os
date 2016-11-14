package io.subutai.core.systemmanager.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.subutai.core.systemmanager.impl.entity.UpdateEntity;


public class UpdateDao
{
    private static final Logger LOG = LoggerFactory.getLogger( UpdateDao.class );
    private EntityManagerFactory emf;


    public UpdateDao( final EntityManagerFactory emf )
    {
        Preconditions.checkNotNull( emf );

        this.emf = emf;
    }


    public UpdateEntity getLast()
    {
        EntityManager em = emf.createEntityManager();
        UpdateEntity result = null;
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from UpdateEntity h order by h.updateDate desc fetch first row only",
                    UpdateEntity.class ).getSingleResult();
            em.getTransaction().commit();
        }
        catch ( NoResultException ne )
        {
            LOG.warn( "No update record found" );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }

        return result;
    }


    public List<UpdateEntity> getLast( int lastN )
    {
        List<UpdateEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery(
                    "select h from UpdateEntity h order by h.updateDate desc fetch first :lastN rows only",
                    UpdateEntity.class ).setParameter( "lastN", lastN ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
        return result;
    }


    public void persist( final UpdateEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
            em.refresh( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }


    public void update( final UpdateEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.merge( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            em.close();
        }
    }
}
