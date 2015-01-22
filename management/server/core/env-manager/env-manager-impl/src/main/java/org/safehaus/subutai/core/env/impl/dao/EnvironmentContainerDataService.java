package org.safehaus.subutai.core.env.impl.dao;


import java.util.Collection;
import javax.persistence.EntityManager;
import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentContainerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class EnvironmentContainerDataService implements DataService<String, EnvironmentContainerImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentContainerDataService.class );
    private DaoManager daoManager;

    public EnvironmentContainerDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }

    @Override
    public EnvironmentContainerImpl find( final String id )
    {
        EnvironmentContainerImpl result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( EnvironmentContainerImpl.class, id );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        finally
        {
            daoManager.closeEntityManager(em);
        }
        return result;
    }


    @Override
    public Collection<EnvironmentContainerImpl> getAll()
    {
        Collection<EnvironmentContainerImpl> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
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
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    @Override
    public void persist( final EnvironmentContainerImpl item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.persist( item );
            em.flush();
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            EnvironmentContainerImpl item = em.find( EnvironmentContainerImpl.class, id );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    @Override
    public void update( final EnvironmentContainerImpl item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            em.merge( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            daoManager.rollBackTransaction( em );
        }
        finally
        {
            daoManager.closeEntityManager(em);
        }
    }
}
