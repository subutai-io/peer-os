package org.safehaus.subutai.core.environment.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;


import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.environment.impl.entity.EnvironmentContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class EnvironmentContainerDataService implements DataService<String, EnvironmentContainerImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentContainerDataService.class );
    EntityManager em;


    public EnvironmentContainerDataService( EntityManager entityManager )
    {
        this.em = entityManager;
    }


    public void setEntityManager( final EntityManager em )
    {
        this.em = em;
    }


    @Override
    public EnvironmentContainerImpl find( final String id )
    {
        EnvironmentContainerImpl result = null;

        try
        {
             result = em.find( EnvironmentContainerImpl.class, id );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }

        return result;
    }


    @Override
    public Collection<EnvironmentContainerImpl> getAll()
    {
        Collection<EnvironmentContainerImpl> result = Lists.newArrayList();

        try
        {

            result = em.createQuery( "select h from EnvironmentContainerImpl h", EnvironmentContainerImpl.class )
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
    public void persist( final EnvironmentContainerImpl item )
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
            EnvironmentContainerImpl item = em.find( EnvironmentContainerImpl.class, id );
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
    public void update( final EnvironmentContainerImpl item )
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
