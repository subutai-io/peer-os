package io.subutai.core.hubmanager.api.dao;


import java.util.List;

import io.subutai.core.hubmanager.api.model.ContainerMetrics;


public interface ContainerMetricsService
{

    ContainerMetrics getMetrics( long id );

    ContainerMetrics getMetricsByContainerId( String id );

    List<ContainerMetrics> getAll();

    void persistMetrics( ContainerMetrics item );

    void removeMetrics( long id );

    void updateMetrics( ContainerMetrics item );
}
