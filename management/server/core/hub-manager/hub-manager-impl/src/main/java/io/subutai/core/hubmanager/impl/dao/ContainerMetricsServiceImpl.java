package io.subutai.core.hubmanager.impl.dao;


import java.util.List;

import com.google.common.base.Preconditions;

import io.subutai.common.dao.DaoManager;
import io.subutai.core.hubmanager.api.dao.ContainerMetricsService;
import io.subutai.core.hubmanager.api.model.ContainerMetrics;


public class ContainerMetricsServiceImpl implements ContainerMetricsService
{
    private ContainerMetricsDAO containerMetricsDAO = null;


    public ContainerMetricsServiceImpl( final DaoManager daoManager )
    {
        Preconditions.checkNotNull( daoManager );

        containerMetricsDAO = new ContainerMetricsDAO( daoManager );
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
}
