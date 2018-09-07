package io.subutai.core.bazaarmanager.api.dao;


import java.util.List;

import io.subutai.core.bazaarmanager.api.model.ContainerMetrics;


public interface ContainerMetricsService
{

    List<ContainerMetrics> getOldest( int limit );

    void save( ContainerMetrics item );

    void removeMetrics( long id );

    void purgeOldMetrics( int daysOld );
}
