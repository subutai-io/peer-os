package io.subutai.core.bazaarmanager.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaarmanager.api.model.ContainerMetrics;
import io.subutai.core.bazaarmanager.impl.model.ContainerMetricsEntity;


/**
 * Implementation of Container host metrics DAO
 */
class ContainerMetricsDAO
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerMetrics.class );

    private DaoManager daoManager = null;


    /* *************************************************
     *
     */
    ContainerMetricsDAO( final DaoManager daoManager )
    {
        this.daoManager = daoManager;
    }


    /* *************************************************
     *
     */
    List<ContainerMetrics> getOldest( int limit )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<ContainerMetrics> result = Lists.newArrayList();
        try
        {
            daoManager.startTransaction( em );

            result = em.createQuery( "select cm from ContainerMetricsEntity cm order by cm.createDate asc",
                    ContainerMetrics.class ).setMaxResults( limit ).getResultList();

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
        return result;
    }


    /* *************************************************
     *
     */
    void persist( ContainerMetrics item )
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
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    void remove( final Long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );

            ContainerMetrics item = em.find( ContainerMetricsEntity.class, id );
            em.remove( item );

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    void purgeOldMetrics( int daysOld )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        try
        {
            daoManager.startTransaction( em );

            long ts = System.currentTimeMillis();

            Query query = em.createQuery(
                    "delete from ContainerMetricsEntity e where e.createDate + 3600 * 1000 * 24 * :days < :ts1" )
                            .setParameter( "days", daysOld ).setParameter( "ts1", ts );

            query.executeUpdate();

            daoManager.commitTransaction( em );
        }
        catch ( Exception e )
        {
            daoManager.rollBackTransaction( em );

            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }
}
