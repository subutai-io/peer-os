package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.entity.ResourceHostEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class ResourceHostDataService implements DataService<String, ResourceHostEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( ResourceHostDataService.class );
    EntityManager em;


    public ResourceHostDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em )
    {
        this.em = em;
    }


    @Override
    public ResourceHostEntity find( final String id )
    {
        ResourceHostEntity result = null;
        try
        {
            result = em.find( ResourceHostEntity.class, id );
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
    public Collection<ResourceHostEntity> getAll()
    {
        Collection<ResourceHostEntity> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from ResourceHostEntity h", ResourceHostEntity.class ).getResultList();
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
    public void persist( final ResourceHostEntity item )
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
            ResourceHostEntity item = em.find( ResourceHostEntity.class, id );
            em.remove( item );
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
    public void update( final ResourceHostEntity item )
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
