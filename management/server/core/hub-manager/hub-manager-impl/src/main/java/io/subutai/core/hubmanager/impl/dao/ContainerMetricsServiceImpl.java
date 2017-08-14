package io.subutai.core.hubmanager.impl.dao;


import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubmanager.api.dao.ContainerMetricsService;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;


public class ContainerMetricsServiceImpl implements ContainerMetricsService
{
    private static final Logger LOG = LoggerFactory.getLogger( ContainerMetricsServiceImpl.class );
    private ContainerMetricsDAO containerMetricsDAO = null;


    public ContainerMetricsServiceImpl( final DaoManager daoManager )
    {
        if ( daoManager != null )
        {
            containerMetricsDAO = new ContainerMetricsDAO( daoManager );
        }
        else
        {
            LOG.error( "*** ContainerMetricsServiceImpl DaoManager is NULL.  ***" );
        }
    }


    @Override
    public ContainerMetrics getMetrics( final long id )
    {
        return containerMetricsDAO.find( id );
    }


    @Override
    public ContainerMetrics getMetricsByContainerId( final String id )
    {
        return containerMetricsDAO.getByContainerId( id );
    }


    @Override
    public List<ContainerMetrics> getAll()
    {
        return containerMetricsDAO.getAll();
    }


    @Override
    public void save( final ContainerMetrics item )
    {
        containerMetricsDAO.persist( item );
    }


    @Override
    public void removeMetrics( final long id )
    {
        containerMetricsDAO.remove( id );
    }


    @Override
    public void updateMetrics( final ContainerMetrics item )
    {
        containerMetricsDAO.update( item );
    }
}
