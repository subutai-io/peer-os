package io.subutai.core.localpeer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.peer.EnvironmentId;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.localpeer.impl.entity.TunnelEntity;


public class TunnelDataService implements DataService<Long, TunnelEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( TunnelDataService.class );
    private EntityManagerFactory emf;


    public TunnelDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    @Override
    public TunnelEntity find( final Long id )
    {
        TunnelEntity result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( TunnelEntity.class, id );
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
    public Collection<TunnelEntity> getAll()
    {
        Collection<TunnelEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from TunnelEntity h", TunnelEntity.class ).getResultList();
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
    public void persist( final TunnelEntity item )
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
    public void remove( final Long id )
    {
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            TunnelEntity item = em.find( TunnelEntity.class, id );
            em.remove( item );
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
    public void update( TunnelEntity item )
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


    public TunnelEntity saveOrUpdate( TunnelEntity item )
    {
        EntityManager em = emf.createEntityManager();

        try
        {

            em.getTransaction().begin();
            if ( em.find( TunnelEntity.class, item.getId() ) == null )
            {
                em.persist( item );
                em.refresh( item );
            }
            else
            {
                item = em.merge( item );
            }
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

        return item;
    }


    public Collection<TunnelEntity> findByEnvironmentId( final EnvironmentId environmentId )
    {
        Collection<TunnelEntity> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            result = em.createQuery( "select t from TunnelEntity t where t.environmentId = :environmentId",
                    TunnelEntity.class ).setParameter( "environmentId", environmentId.getId() ).getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
            em.close();
        }
        return result;
    }
}
