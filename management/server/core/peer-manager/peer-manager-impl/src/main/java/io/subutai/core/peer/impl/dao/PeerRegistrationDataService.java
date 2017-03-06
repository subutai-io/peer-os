package io.subutai.core.peer.impl.dao;


import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.peer.RegistrationData;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.peer.impl.entity.PeerRegistrationData;


public class PeerRegistrationDataService implements DataService<String, PeerRegistrationData>
{
    private static final Logger LOG = LoggerFactory.getLogger( PeerRegistrationDataService.class );
    EntityManagerFactory emf;


    public PeerRegistrationDataService( EntityManagerFactory entityManagerFactory )
    {
        this.emf = entityManagerFactory;
    }


    @Override
    public PeerRegistrationData find( final String id )
    {
        PeerRegistrationData result = null;
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.find( PeerRegistrationData.class, id );
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


    public List<RegistrationData> getAllData()
    {
        List<RegistrationData> datas = Lists.newArrayList();

        Collection<PeerRegistrationData> peerRegistrationDatas = getAll();

        for ( PeerRegistrationData peerRegistrationData : peerRegistrationDatas )
        {
            datas.add( peerRegistrationData.getRegistrationData() );
        }

        return datas;
    }


    @Override
    public Collection<PeerRegistrationData> getAll()
    {
        Collection<PeerRegistrationData> result = Lists.newArrayList();
        EntityManager em = emf.createEntityManager();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from PeerRegistrationData h", PeerRegistrationData.class )
                       .getResultList();
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
    public void persist( final PeerRegistrationData item )
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
            PeerRegistrationData item = em.find( PeerRegistrationData.class, id );
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
    public void update( PeerRegistrationData item )
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


    public void saveOrUpdate( PeerRegistrationData PeerRegistrationData )
    {
        EntityManager em = emf.createEntityManager();

        try
        {

            em.getTransaction().begin();
            if ( em.find( PeerRegistrationData.class, PeerRegistrationData.getId() ) == null )
            {
                em.persist( PeerRegistrationData );
            }
            else
            {
                PeerRegistrationData = em.merge( PeerRegistrationData );
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
