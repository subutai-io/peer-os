package io.subutai.core.registration.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.registration.impl.entity.ContainerTokenImpl;


/**
 * Created by talas on 8/28/15.
 */
public class ContainerTokenDataService implements DataService<String, ContainerTokenImpl>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerTokenDataService.class );
    private DaoManager daoManager;


    public ContainerTokenDataService( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public Collection<ContainerTokenImpl> getAll()
    {
        Collection<ContainerTokenImpl> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from ContainerTokenImpl h", ContainerTokenImpl.class ).getResultList();
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
    public ContainerTokenImpl find( final String id )
    {
        ContainerTokenImpl result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.find( ContainerTokenImpl.class, id );
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
    public void persist( final ContainerTokenImpl item )
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
    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            ContainerTokenImpl item = em.find( ContainerTokenImpl.class, id );
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
    public void update( final ContainerTokenImpl item )
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
