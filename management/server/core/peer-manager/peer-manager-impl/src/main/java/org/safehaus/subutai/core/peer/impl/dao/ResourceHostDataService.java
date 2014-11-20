package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.model.ResourceHostEntity;

import com.google.common.collect.Lists;


public class ResourceHostDataService implements DataService<String, ResourceHostEntity>
{
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
            em.getTransaction().begin();
            result = em.find( ResourceHostEntity.class, id );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        return result;
    }


    @Override
    public Collection<ResourceHostEntity> getAll()
    {
        Collection<ResourceHostEntity> result = Lists.newArrayList();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from ResourceHostEntity h", ResourceHostEntity.class ).getResultList();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
        return result;
    }


    @Override
    public void persist( final ResourceHostEntity item )
    {
        try
        {
            em.getTransaction().begin();
            em.persist( item );
            em.flush();
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
    }


    @Override
    public void remove( final ResourceHostEntity item )
    {
        try
        {
            em.getTransaction().begin();
            em.remove( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
    }


    @Override
    public void update( final ResourceHostEntity item )
    {
        try
        {
            em.getTransaction().begin();
            em.merge( item );
            em.getTransaction().commit();
        }
        catch ( Exception e )
        {
            if ( em.getTransaction().isActive() )
            {
                em.getTransaction().rollback();
            }
        }
    }
}
