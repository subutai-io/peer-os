package io.subutai.core.hubmanager.impl.dao;


import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import io.subutai.common.dao.DaoManager;
import io.subutai.common.exception.ActionFailedException;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;
import io.subutai.core.hubmanager.impl.model.ContainerMetricsEntity;


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
    ContainerMetrics find( final long id )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        ContainerMetrics result = null;
        try
        {
            daoManager.startTransaction( em );
            result = em.find( ContainerMetricsEntity.class, id );
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
    List<ContainerMetrics> getAll()
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();

        List<ContainerMetrics> result = Lists.newArrayList();
        try
        {
            result = em.createQuery( "select cm from ContainerMetrics cm", ContainerMetrics.class )
                       .getResultList();
        }
        catch ( Exception e )
        {
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

            throw new ActionFailedException( e.getMessage() );
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
            ContainerMetrics item = em.find( ContainerMetrics.class, id );
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


    /* *************************************************
     *
     */
    public void update( final ContainerMetrics item )
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
            daoManager.rollBackTransaction( em );

            LOG.error( "Error updating user", e );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }
    }


    /* *************************************************
     *
     */
    public ContainerMetrics getByContainerId( final String containerHostId )
    {
        EntityManager em = daoManager.getEntityManagerFromFactory();
        ContainerMetrics result = null;
        try
        {
            TypedQuery<ContainerMetrics> query =
                    em.createQuery( "select cm from ContainerMetricsEntity cm where cm.hostId = :hostId",
                            ContainerMetrics.class );
            query.setParameter( "hostId", containerHostId );

            List<ContainerMetrics> resultList = query.getResultList();
            if ( !resultList.isEmpty() )
            {
                result = resultList.iterator().next();
            }
        }
        catch ( Exception e )
        {
            LOG.error( e.getMessage() );
        }
        finally
        {
            daoManager.closeEntityManager( em );
        }

        return result;
    }
}
