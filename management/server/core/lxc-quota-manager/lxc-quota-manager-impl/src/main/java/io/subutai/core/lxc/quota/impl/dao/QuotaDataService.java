package io.subutai.core.lxc.quota.impl.dao;


import java.util.Collection;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.protocol.api.DataService;
import io.subutai.core.lxc.quota.impl.entity.QuotaEntity;


/**
 * {@link QuotaDataService} implements {@link DataService} interface. {@link
 * QuotaDataService} manages {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} entities in database
 */
public class QuotaDataService implements DataService<String, QuotaEntity>
{
    private static final Logger LOG = LoggerFactory.getLogger( QuotaDataService.class );
    private DaoManager daoManager;


    public QuotaDataService( DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /**
     * Returns {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} object for requested id {@link String}
     * <p>@param id - entity id to retrieve an object from database</p> <p>@return - {@link
     * io.subutai.core.lxc.quota.impl.entity.QuotaEntity} object or {@code null} value</p>
     */
    @Override
    public QuotaEntity find( final String id )
    {
        QuotaEntity result = null;
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.find( QuotaEntity.class, id );
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
     * Gets list of all {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} exist in database
     *
     * @return - {@link Collection} of {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity}
     */
    @Override
    public Collection<QuotaEntity> getAll()
    {
        Collection<QuotaEntity> result = Lists.newArrayList();
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            result = em.createQuery( "select e from QuotaEntity e" ).getResultList();
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
     * Save {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} object to database <b>Warning your entity
     * object key must be unique in database otherwise rollback transaction will be applied </b>
     *
     * @param item - entity object to save
     */
    @Override
    public void persist( final QuotaEntity item )
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
     * Delete {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} from database by {@link String} key
     *
     * @param id - entity id to remove
     */
    @Override
    public void remove( final String id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            QuotaEntity item = em.find( QuotaEntity.class, id );

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


    public void remove( QuotaEntity item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        try
        {
            daoManager.startTransaction( em );
            item = em.find( QuotaEntity.class, item.getContainerId() );
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
     * Update {@link io.subutai.core.lxc.quota.impl.entity.QuotaEntity} entity saved in database
     *
     * @param item - entity to update
     */
    @Override
    public void update( QuotaEntity item )
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


    public  QuotaEntity saveOrUpdate( QuotaEntity item )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );
            if ( em.find( QuotaEntity.class, item.getContainerId() ) == null )
            {
                em.persist( item );
                em.refresh( item );
            }
            else
            {
                item = em.merge( item );
            }
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

        return ( QuotaEntity ) item;
    }
}
