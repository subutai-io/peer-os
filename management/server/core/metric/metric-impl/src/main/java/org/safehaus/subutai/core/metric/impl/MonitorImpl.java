package org.safehaus.subutai.core.metric.impl;


import java.util.Set;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;

import com.google.common.base.Preconditions;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    private final MonitorDao monitorDao;


    public MonitorImpl( final DataSource dataSource ) throws DaoException
    {
        Preconditions.checkNotNull( dataSource, "Data source is null" );

        this.monitorDao = new MonitorDao( dataSource );
    }


    @Override
    public Set<ContainerHostMetric> getContainerMetrics( final Environment environment )
    {
        //check if environment exists
        //iterate containers within the environment and get their metrics
        return null;
    }


    @Override
    public Set<ResourceHostMetric> getResourceHostMetrics()
    {
        //iterate resource hosts and get their metrics
        return null;
    }


    @Override
    public void startMonitoring( final MetricListener metricListener, final Environment environment )
    {
        //save subscription to database
        //make sure subscriber id is truncated to 100 characters
    }


    @Override
    public void stopMonitoring( final MetricListener metricListener, final Environment environment )
    {

    }
}
