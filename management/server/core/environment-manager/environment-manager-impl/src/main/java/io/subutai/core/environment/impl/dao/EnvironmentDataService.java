package io.subutai.core.environment.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.environment.Environment;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.environment.impl.entity.EnvironmentImpl;


/**
 * {@link EnvironmentDataService} implements {@link io.subutai.common.protocol.api.DataService} interface. {@link
 * EnvironmentDataService} manages {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} entities in database
 */
public class EnvironmentDataService implements DataService<String, EnvironmentImpl>
{
    private static final Logger LOG = LoggerFactory.getLogger( EnvironmentDataService.class );
    private DaoManager daoManager;
    private EntityManager em;


    public EnvironmentDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
        this.em = daoManager.getEntityManagerFromFactory();
    }


    /**
     * Returns {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} object for requested id {@link String}
     * <p>@param id - entity id to retrieve an object from database</p> <p>@return - {@link
     * io.subutai.core.environment.impl.entity.EnvironmentImpl} object or {@code null} value</p>
     */
    @Override
    public EnvironmentImpl find( final String id )
    {
        EnvironmentImpl result = null;
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( EnvironmentImpl.class, id );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
        return result;
    }


    /**
     * Gets list of all {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} exist in database
     *
     * @return - {@link java.util.Collection} of {@link io.subutai.core.environment.impl.entity.EnvironmentImpl}
     */
    @Override
    public Collection<EnvironmentImpl> getAll()
    {
        Collection<EnvironmentImpl> result = Lists.newArrayList();
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select h from EnvironmentImpl h", EnvironmentImpl.class ).getResultList();
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
        }
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
        return result;
    }


    /**
     * Save {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} object to database <b>Warning your entity
     * object key must be unique in database otherwise rollback transaction will be applied </b>
     *
     * @param item - entity object to save
     */
    @Override
    public void persist( final EnvironmentImpl item )
    {
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
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
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
    }


    /**
     * Delete {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} from database by {@link String} key
     *
     * @param id - entity id to remove
     */
    @Override
    public void remove( final String id )
    {
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
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
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
    }


    public void remove( EnvironmentImpl item )
    {
        try
        {
            daoManager.startTransaction( em );
            em.remove( item );
            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            daoManager.rollBackTransaction( em );
        }
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
    }


    /**
     * Update {@link io.subutai.core.environment.impl.entity.EnvironmentImpl} entity saved in database
     *
     * @param item - entity to update
     */
    @Override
    public void update( EnvironmentImpl item )
    {
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
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
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
    }


    public EnvironmentImpl saveOrUpdate( EnvironmentImpl item )
    {
        //        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            if ( em.contains( item ) )
            {
                em.merge( item );
            }
            else
            {
                em.persist( item );
            }
            daoManager.commitTransaction( em );
            em.refresh( item );
        }
        catch ( Exception e )
        {
            LOG.error( e.toString(), e );
            daoManager.rollBackTransaction( em );
        }
        //        finally
        //        {
        //            daoManager.closeEntityManager( em );
        //        }
        return item;
    }
}
