package io.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.peer.Peer;
import io.subutai.common.protocol.api.DataService;
import io.subutai.common.util.JsonUtil;
import io.subutai.core.peer.impl.entity.PeerData;


public class PeerDataService implements DataService<String, PeerData>
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerDataService.class );
    EntityManagerFactory emf;


    public PeerDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    @Override
    public PeerData find( final String id )
    {
        PeerData result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( PeerData.class, id );
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
    public Collection<PeerData> getAll()
    {
        Collection<PeerData> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from PeerData h", PeerData.class ).getResultList();
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
    public void persist( final PeerData item )
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
            PeerData item = em.find( PeerData.class, id );
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
    public void update( PeerData item )
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


    public void saveOrUpdate( PeerData peerData )
    {
        EntityManager em = emf.createEntityManager();

        try
        {

            em.getTransaction().begin();
            if ( em.find( PeerData.class, peerData.getId() ) == null )
            {
                em.persist( peerData );
            }
            else
            {
                peerData = em.merge( peerData );
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
    }
}
