package org.safehaus.subutai.core.metric.impl;


import java.util.Set;

import javax.sql.DataSource;

import org.safehaus.subutai.common.exception.DaoException;
import org.safehaus.subutai.core.environment.api.helper.Environment;
import org.safehaus.subutai.core.metric.api.ContainerHostMetric;
import org.safehaus.subutai.core.metric.api.MetricListener;
import org.safehaus.subutai.core.metric.api.Monitor;
import org.safehaus.subutai.core.metric.api.ResourceHostMetric;
import org.safehaus.subutai.core.monitor.api.MonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;


/**
 * Implementation of Monitor
 */
public class MonitorImpl implements Monitor
{
    protected static final Logger LOG = LoggerFactory.getLogger( MonitorImpl.class.getName() );

    private static final int MAX_SUBSCRIBER_ID_LEN = 100;
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
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, "Metric listener is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        //save subscription to database
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
        if ( subscriberId.length() > MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, MAX_SUBSCRIBER_ID_LEN );
        }
        try
        {
            monitorDao.addSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in startMonitoring", e );
            throw new MonitorException( e );
        }
    }


    @Override
    public void stopMonitoring( final MetricListener metricListener, final Environment environment )
            throws MonitorException
    {
        Preconditions.checkNotNull( metricListener, "Metric listener is null" );
        Preconditions.checkNotNull( environment, "Environment is null" );
        //remove subscription from database
        //make sure subscriber id is truncated to 100 characters
        String subscriberId = metricListener.getSubscriberId();
        if ( subscriberId.length() > MAX_SUBSCRIBER_ID_LEN )
        {
            subscriberId = subscriberId.substring( 0, MAX_SUBSCRIBER_ID_LEN );
        }
        try
        {
            monitorDao.removeSubscription( environment.getId(), subscriberId );
        }
        catch ( DaoException e )
        {
            LOG.error( "Error in stopMonitoring", e );
            throw new MonitorException( e );
        }
    }
}
