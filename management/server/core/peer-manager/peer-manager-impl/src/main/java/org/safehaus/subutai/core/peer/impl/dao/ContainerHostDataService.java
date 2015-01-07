package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.entity.ContainerHostEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class ContainerHostDataService implements DataService<String, ContainerHostEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerHostDataService.class );
    EntityManager em;


    public ContainerHostDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em )
    {
        this.em = em;
    }


    @Override
    public ContainerHostEntity find( final String id )
    {
        ContainerHostEntity result = null;
        try
        {
            result = em.find( ContainerHostEntity.class, id );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
        return result;
    }


    @Override
    public Collection<ContainerHostEntity> getAll()
    {
        Collection<ContainerHostEntity> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from ContainerHostEntity h", ContainerHostEntity.class ).getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
        return result;
    }


    @Override
    public void persist( final ContainerHostEntity item )
    {
        try
        {
            em.persist( item );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    @Override
    public void remove( final String id )
    {
        try
        {
            ContainerHostEntity entity = em.find( ContainerHostEntity.class, id );
            em.remove( entity );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }


    @Override
    public void update( final ContainerHostEntity item )
    {
        try
        {
            em.merge( item );
            em.flush();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
        }
    }
}
