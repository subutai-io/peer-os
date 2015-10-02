package io.subutai.core.registration.impl.dao;


import java.util.Collection;
import java.util.UUID;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.registration.impl.entity.RequestedHostImpl;


public class RequestDataService implements DataService<UUID, RequestedHostImpl>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( RequestDataService.class );

    private DaoManager daoManager;


    public RequestDataService( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public Collection<RequestedHostImpl> getAll()
    {
        Collection<RequestedHostImpl> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from RequestedHostImpl h", RequestedHostImpl.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString(), e );
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
    public RequestedHostImpl find( final UUID id )
    {
        RequestedHostImpl result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.find( RequestedHostImpl.class, id.toString() );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString(), e );
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
    public void persist( final RequestedHostImpl item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString(), e );
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
    public void remove( final UUID id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            RequestedHostImpl item = em.find( RequestedHostImpl.class, id.toString() );
            em.remove( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString(), e );
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
    public void update( final RequestedHostImpl item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            em.merge( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            LOGGER.error( e.toString(), e );
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
