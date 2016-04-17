package io.subutai.core.registration.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.registration.impl.entity.ContainerInfoImpl;



public class ContainerInfoDataService implements DataService<String, ContainerInfoImpl>
{
    private static final Logger LOGGER = LoggerFactory.getLogger( ContainerInfoDataService.class );

    private DaoManager daoManager;


    public ContainerInfoDataService( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public Collection<ContainerInfoImpl> getAll()
    {
        Collection<ContainerInfoImpl> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from ContainerInfoImpl h", ContainerInfoImpl.class ).getResultList();
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
    public ContainerInfoImpl find( final String id )
    {
        ContainerInfoImpl result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            em.getTransaction().begin();
            result = em.find( ContainerInfoImpl.class, id );
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
    public void persist( final ContainerInfoImpl item )
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
            ContainerInfoImpl item = em.find( ContainerInfoImpl.class, id );
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
    public void update( final ContainerInfoImpl item )
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
