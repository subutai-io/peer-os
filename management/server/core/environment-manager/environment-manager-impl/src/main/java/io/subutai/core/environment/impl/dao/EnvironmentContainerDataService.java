package io.subutai.core.environment.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.environment.impl.entity.EnvironmentContainerImpl;


/**
 * {@link EnvironmentContainerDataService} manages information about
 * environment container in database
 */
public class EnvironmentContainerDataService implements DataService<String, EnvironmentContainerImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentContainerDataService.class );
    private DaoManager daoManager;

    public EnvironmentContainerDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /**
     * Returns {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} object for requested id {@link java.lang.String}
     * <p>@param id - entity id to retrieve an object from database</p>
     * <p>@return - {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} object or {@code null} value</p>
     */
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


    /**
     * Gets list of all {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} exist in database
     * @return - {@link java.util.Collection} of {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl}
     */
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


    /**
     * Save {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} object to database <b>Warning your entity object
     * key must be unique in database otherwise rollback transaction will be applied </b>
     * @param item - entity object to save
     */
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

    /**
     * Delete {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} from database by {@link java.lang.String} key
     * @param id - entity id to remove
     */
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


    /**
     * Update {@link io.subutai.core.environment.impl.entity.EnvironmentContainerImpl} entity saved in database
     * @param item - entity to update
     */
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
