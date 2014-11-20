package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.model.ManagementHostEntity;

import com.google.common.collect.Lists;


public class ManagementHostDataService implements DataService<String, ManagementHostEntity>
{
    EntityManager em;


    public ManagementHostDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em )
    {
        this.em = em;
    }


    @Override
    public ManagementHostEntity find( final String id )
    {
        ManagementHostEntity result = null;
        try
        {
            em.getTransaction().begin();
            result = em.find( ManagementHostEntity.class, id );
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
    public Collection<ManagementHostEntity> getAll()
    {
        Collection<ManagementHostEntity> result = Lists.newArrayList();
        try
        {
            em.getTransaction().begin();
            result = em.createQuery( "select h from ManagementHostEntity h", ManagementHostEntity.class )
                       .getResultList();
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
    public void persist( final ManagementHostEntity item )
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
    public void remove( final ManagementHostEntity item )
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
    public void update( final ManagementHostEntity item )
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
