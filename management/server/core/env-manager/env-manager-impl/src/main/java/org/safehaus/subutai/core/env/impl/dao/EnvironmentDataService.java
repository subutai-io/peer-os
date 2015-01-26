package org.safehaus.subutai.core.env.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.safehaus.subutai.common.dao.DaoManager;
import org.safehaus.subutai.common.protocol.api.DataService;
import org.safehaus.subutai.core.env.impl.entity.EnvironmentImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;


public class EnvironmentDataService implements DataService<String, EnvironmentImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDataService.class );
    private DaoManager daoManager;


    public EnvironmentDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    @Override
    public EnvironmentImpl find( final String id )
    {
        EnvironmentImpl result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( EnvironmentImpl.class, id );
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
    public Collection<EnvironmentImpl> getAll()
    {
        Collection<EnvironmentImpl> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
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
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    @Override
    public void persist( final EnvironmentImpl item )
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
            EnvironmentImpl item = em.find( EnvironmentImpl.class, id );

            daoManager.startTransaction( em );
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
    public void update( final EnvironmentImpl item )
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
            daoManager.closeEntityManager( em );
        }
    }
}
