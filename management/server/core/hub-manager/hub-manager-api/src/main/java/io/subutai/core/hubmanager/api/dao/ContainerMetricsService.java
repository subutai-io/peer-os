package io.subutai.core.hubmanager.api.dao;


import java.util.List;

import io.subutai.core.hubmanager.api.model.ContainerMetrics;


public interface ContainerMetricsService
{

    List<ContainerMetrics> getAll();

    void save( ContainerMetrics item );

    void removeMetrics( long id );

    void purgeOldMetrics();
}
