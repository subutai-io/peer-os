package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.model.ContainerHostEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class ContainerHostDataService implements DataService<String, ContainerHostEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerHostDataService.class );
    EntityManagerFactory emf;


    public ContainerHostDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    public void setEntityManagerFactory( final EntityManagerFactory emf )
    {
        this.emf = emf;
    }


    @Override
    public ContainerHostEntity find( final String id )
    {
        ContainerHostEntity result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( ContainerHostEntity.class, id );
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


    @Override
    public Collection<ContainerHostEntity> getAll()
    {
        Collection<ContainerHostEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from ContainerHostEntity h", ContainerHostEntity.class ).getResultList();
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


    @Override
    public void persist( final ContainerHostEntity item )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
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


    @Override
    public void remove( final String id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            ContainerHostEntity entity = em.find( ContainerHostEntity.class, id );
            em.remove( entity );
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


    @Override
    public void update( final ContainerHostEntity item )
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
