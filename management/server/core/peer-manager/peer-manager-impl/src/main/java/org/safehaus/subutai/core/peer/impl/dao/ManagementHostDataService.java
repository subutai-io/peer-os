package org.safehaus.subutai.core.peer.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.peer.impl.entity.ManagementHostEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class ManagementHostDataService implements DataService<String, ManagementHostEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( ManagementHostDataService.class );
    EntityManager em;


    public ManagementHostDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em)
    {
        this.em = em;
    }


    @Override
    public ManagementHostEntity find( final String id )
    {
        ManagementHostEntity result = null;
        try
        {
            result = em.find( ManagementHostEntity.class, id );
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
    public Collection<ManagementHostEntity> getAll()
    {
        Collection<ManagementHostEntity> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select h from ManagementHostEntity h", ManagementHostEntity.class )
                       .getResultList();
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
    public void persist( final ManagementHostEntity item )
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
            ManagementHostEntity item = em.find( ManagementHostEntity.class, id );
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
    public void update( final ManagementHostEntity item )
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
