package org.safehaus.subutai.core.environment.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class EnvironmentDataService implements DataService<String, EnvironmentImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDataService.class );
    EntityManager em;


    public EnvironmentDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em )
    {
        this.em = em;
    }


    @Override
    public EnvironmentImpl find( final String id )
    {
        EnvironmentImpl result = null;

        try
        {
            result = em.find( EnvironmentImpl.class, id );
            em.flush();
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
    public Collection<EnvironmentImpl> getAll()
    {
        Collection<EnvironmentImpl> result = Lists.newArrayList();

        try
        {
            result = em.createQuery( "select h from EnvironmentImpl h", EnvironmentImpl.class ).getResultList();
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
    public void persist( final EnvironmentImpl item )
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
            EnvironmentImpl item = em.find( EnvironmentImpl.class, id );
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
    public void update( final EnvironmentImpl item )
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
