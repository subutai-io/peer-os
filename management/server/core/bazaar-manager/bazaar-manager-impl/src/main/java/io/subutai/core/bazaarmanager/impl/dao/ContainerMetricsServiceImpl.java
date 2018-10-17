package io.subutai.core.bazaarmanager.impl.dao;


import java.util.List;

import com.google.common.base.Preconditions;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.bazaarmanager.api.dao.ContainerMetricsService;
import io.subutai.core.bazaarmanager.api.model.ContainerMetrics;


public class ContainerMetricsServiceImpl implements ContainerMetricsService
{
    private ContainerMetricsDAO containerMetricsDAO = null;


    public ContainerMetricsServiceImpl( final DaoManager daoManager )
    {
        Preconditions.checkNotNull( daoManager );

        containerMetricsDAO = new ContainerMetricsDAO( daoManager );
    }


    @Override
    public List<ContainerMetrics> getOldest( int limit )
    {
        return containerMetricsDAO.getOldest( limit );
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
    public void purgeOldMetrics( int daysOld )
    {
        containerMetricsDAO.purgeOldMetrics( daysOld );
    }
}
